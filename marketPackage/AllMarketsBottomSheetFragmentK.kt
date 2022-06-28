package com.arsinex.com.marketPackage

import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinex.com.Objects.MarketObject
import com.arsinex.com.RequestUrls
import com.arsinex.com.databinding.FragmentAllMarketsBinding
import com.arsinex.com.enums.MarketFragments
import com.arsinex.com.enums.RequestType
import com.arsinex.com.utilitiesPackage.Utilities
import com.github.mikephil.charting.data.Entry
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Request
import okhttp3.internal.EMPTY_REQUEST
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class AllMarketsBottomSheetFragmentK : BottomSheetDialogFragment(), AllMarketsListAdaptor.OnItemClickListener{

    private companion object {
        const val MARKET_TAG = "market_tag"
        const val DAY_IN_SECONDS = 24 * 60 * 60
        const val CHART_TICK_INTERVALS = 1800 // every half an hour
    }

    private lateinit var binding: FragmentAllMarketsBinding

    private lateinit var allMarketsListAdaptor: AllMarketsListAdaptor

    private val marketsDictionary = LinkedHashMap<String, MarketObject>()
    private var marketsList: ArrayList<MarketObject>? = ArrayList<MarketObject>()
    private val marketsListFiltered = ArrayList<MarketObject>()

    private val marketViewModel: MarketViewModel by activityViewModels()

    private var filterIsApplied = false

    private val utilities = Utilities()
    private val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllMarketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.txtSearch.isEnabled = false

        allMarketsListAdaptor = AllMarketsListAdaptor(requireActivity(), marketsListFiltered, this)

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        with(binding.recycleAllMarketList) {
            setLayoutManager(layoutManager)
            itemAnimator = DefaultItemAnimator()
            adapter = allMarketsListAdaptor
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                marketsListFiltered.clear()
                when(charSequence?.length) {
                    0 -> {
                        filterIsApplied = false
                        marketsList?.let { marketsListFiltered.addAll(it) }
                    }
                    else -> {
                        filterIsApplied = true
                        filterResult(charSequence!!)
                    }
                }
                allMarketsListAdaptor.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        listenForViewModelDataChanges()

    }

    override fun onAllMarketItemClick(item: MarketObject?) {
        marketViewModel.setMarket(item)
        marketViewModel.setFragment(MarketFragments.EXCHANGE)
        this.dismiss()
    }

    private fun listenForViewModelDataChanges() {

        // listens for market socket data
        marketViewModel.socketResponse.observe(viewLifecycleOwner, { marketData: String ->
            if (!marketsDictionary.isNullOrEmpty()) {
                with(JSONObject(marketData)) {
                    if (!has("error") && getString("method") == "price.update") {
                        val marketName = updateMarketsDictionary(marketData)
                        updateMarketsListPrice(marketName)
                        updateMarketsAdaptor(marketName)
                        if(!binding.txtSearch.isEnabled) {
                            binding.txtSearch.isEnabled = true
                        }
                    }
                    if (has("result") && getJSONArray("result") is JSONArray) {
                        val moneyName = updateMarketChartInfo(marketData)
                        updateMarketsAdaptor(moneyName)
                    }
                }
            }
            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false)
        })

        // listens for api response data
        marketViewModel.apiResponse.observe(viewLifecycleOwner, { responsePair: Pair<String, RequestType> ->
            val response = responsePair.first
            val requestType = responsePair.second

            if (requestType == RequestType.MARKET_LIST) {
                marketsList?.clear()
                marketsList = parseMarketList(response)
                saveMarketsIntoPreferences(marketsList!!)
                createMarketsDictionary(marketsList!!)
                fetchMarketsInfo()
                fetchMarketsChartInfo()
            }
            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false)
        })

        marketViewModel.isRequestFailed.observe(viewLifecycleOwner, { failurePair: Pair<Boolean?, RequestType> ->
            val requestType = failurePair.second
            if (requestType == RequestType.MARKET_LIST) { getMarketList() }
            // dismiss the loading dialog
            marketViewModel.setShowWaitingBar(false)
        })
    }

    @Throws(JSONException::class)
    private fun parseMarketList(response: String): ArrayList<MarketObject> {
        val list = ArrayList<MarketObject>()
        val arrayResponse = JSONArray(response)
        for (INDEX in 0 until arrayResponse.length()) {
            with(arrayResponse.getJSONObject(INDEX)) {
                list.add(MarketObject(
                    getString("name"),
                    getString("stock"),
                    getString("money"),
                    getString("money_prec"),
                    getString("stock_prec")
                ))
            }
        }
        return list
    }

    private fun loadMarketsFromPreferences(): ArrayList<MarketObject>? {
        val savedJSONString: String? = utilities.getFromSharedPreferences(requireActivity(), "market_dict")

        // checking below if the saved list exists or not
        return if (savedJSONString.isNullOrEmpty()) {
            null
        } else {
            // determines the type of objects in the array list
            val type = object : TypeToken<ArrayList<MarketObject>?>() {}.type
            // in below line we are getting data from gson and saving it to our array list
            gson.fromJson<ArrayList<MarketObject>>(savedJSONString, type)
        }
    }

    private fun saveMarketsIntoPreferences(list: ArrayList<MarketObject>) {
        // getting data from gson and storing it in a string.
        val stringJSON: String = gson.toJson(list)
        utilities.saveInSharedPreferences(requireActivity(), "market_dict", stringJSON)
    }

    private fun getMarketList() {
        val url = RequestUrls().getUrl(RequestType.MARKET_LIST)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(requireActivity(), "token"))
            .post(EMPTY_REQUEST)
            .tag(MARKET_TAG)
            .build()
        marketViewModel.setApiRequest(request, RequestType.MARKET_LIST)
    }

    private fun createMarketsDictionary(list: ArrayList<MarketObject>) {
        marketsDictionary.clear()
        for (market in list) {
            marketsDictionary[market.name] = market
        }
    }

    @Throws(JSONException::class)
    private fun fetchMarketsInfo() {
        val marketJSONArray = JSONArray()
        for (market in marketsList!!) { marketJSONArray.put(market.name) }

        val jsonRequest = JSONObject()
            .put("id", 1)
            .put("method", "price.subscribe")
            .put("params", marketJSONArray)

        marketViewModel.setSocketRequest(jsonRequest.toString())
    }

    @Throws(JSONException::class)
    private fun fetchMarketsChartInfo() {
        val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
        for (market in marketsList!!) {

            val paramsArray = JSONArray()
                .put(market.name)
                .put(currentTime - DAY_IN_SECONDS) // start time
                .put(currentTime) // end time
                .put(CHART_TICK_INTERVALS)

            val jsonRequest = JSONObject()
                .put("id", 1)
                .put("method", "kline.query")
                .put("params", paramsArray)

            marketViewModel.setSocketRequest(jsonRequest.toString())
        }
    }


    @Throws(JSONException::class, NullPointerException::class)
    private fun updateMarketsDictionary(response: String): String? {
        val moneyName = JSONObject(response).getJSONArray("params").getString(0)
        val price = JSONObject(response).getJSONArray("params").getString(1)
        marketsDictionary[moneyName]?.let {
            it.price = price
            return moneyName
        }
        return null
    }

    private fun updateMarketsListPrice(moneyName: String?) {
        moneyName?.let {
            val index = ArrayList(marketsDictionary.keys).indexOf(moneyName)
            if (index == -1) {
                marketsList!![index].price = marketsDictionary[moneyName]!!.price
            }
        }
    }

    private fun updateMarketsAdaptor(moneyName: String?) {
        moneyName?.let { name ->
            marketsListFiltered.clear()
            if (filterIsApplied) {
                filterResult(binding.txtSearch.text.toString())
            } else {
                marketsListFiltered.addAll(marketsList!!)
            }

            // if the arraylist in the adaptor is empty the whole list will be updated
            with(allMarketsListAdaptor) {
                when (itemCount) {
                    0 -> updateDatabase(marketsListFiltered)
                    else -> notifyItemChanged(ArrayList(marketsDictionary.keys).indexOf(name))
                }
            }
        }
    }

    @Throws(JSONException::class)
    private fun updateMarketChartInfo(response: String): String {
        val lineChartDataPoints = ArrayList<Entry>() // Line chart data points
        val resultArray = JSONObject(response).getJSONArray("result")
        for (INDEX in 0 until resultArray.length()) {
            with(resultArray.getJSONArray(INDEX)){
                val average = (this[1].toString().toFloat() + this[2].toString().toFloat()) / 2
                lineChartDataPoints.add(Entry(INDEX.toFloat(), average))
            }
        }
        val moneyName = resultArray.getJSONArray(0)[7].toString()
        marketsList?.get(ArrayList(marketsDictionary.keys).indexOf(moneyName))?.chartData = lineChartDataPoints
        return moneyName
    }

    private fun filterResult(charSequence: CharSequence) {
        for (market in marketsList!!) {
            with(market) {
                if (stock.contains(charSequence, true)) {
                    marketsListFiltered.add(market)
                }
            }
        }
        with(marketsListFiltered) {
            if (isNullOrEmpty()) {
                binding.recycleAllMarketList.visibility = View.GONE
                binding.lblNoAnswer.visibility = View.VISIBLE
            } else {
                binding.recycleAllMarketList.visibility = View.VISIBLE
                binding.lblNoAnswer.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        marketsList?.clear()
        marketsList = loadMarketsFromPreferences()

        if (marketsList.isNullOrEmpty()){
            getMarketList()
        } else {
            createMarketsDictionary(marketsList!!)
            fetchMarketsInfo()
            fetchMarketsChartInfo()
        }
    }


}