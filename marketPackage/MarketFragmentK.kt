package com.arsinex.com.marketPackage

import android.graphics.Color
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinex.com.APIResponseParser
import com.arsinex.com.Objects.MarketObject
import com.arsinex.com.R
import com.arsinex.com.RequestUrls
import com.arsinex.com.databinding.FragmentMarketBinding
import com.arsinex.com.enums.MarketFragments
import com.arsinex.com.enums.RequestType
import com.arsinex.com.utilitiesPackage.Utilities
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Request.Builder
import okhttp3.internal.EMPTY_REQUEST
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MarketFragmentK : Fragment(), MarketsListAdaptor.OnItemClickListener, TrendMarketsAdaptor.OnItemClickListener  {

    private companion object {
        const val TAG = "****************** Market Fragment *****************"
        const val USER_TAG = "user"
        const val MARKET_TAG = "market_list"
        const val BALANCE_TAG = "balance"
        const val ALL_MARKET_TAG = "all_market"
        const val DAY_IN_SECONDS = 24 * 60 * 60
        const val CHART_TICK_INTERVALS = 1800 // every half an hour
    }

    // view
    private lateinit var binding: FragmentMarketBinding

    // view model
    private val marketViewModel: MarketViewModel by activityViewModels()

    // adaptors
    private lateinit var trendMarketsAdaptor: TrendMarketsAdaptor
    private lateinit var marketListAdaptor: MarketsListAdaptor

    // arraylist
    private var marketsList: ArrayList<MarketObject>? = ArrayList<MarketObject>()
    private val marketsDictionary = LinkedHashMap<String, MarketObject>()

    // classes
    private val utilities = Utilities()
    private val gson = Gson()

    // all markets bottom sheet
    private var allMarketsBottomSheetFragment: AllMarketsBottomSheetFragment = AllMarketsBottomSheetFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMarketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lblAllMarket.isClickable = false

        val trendLayoutManager: LinearLayoutManager = CustomLinearLayoutManager(requireContext())
        trendLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recycleTrendMarkets.layoutManager = trendLayoutManager
        binding.recycleTrendMarkets.itemAnimator = DefaultItemAnimator()

        val marketsLayoutManager: LinearLayoutManager = CustomLinearLayoutManager(requireContext())
        marketsLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recycleMarketList.layoutManager = marketsLayoutManager
        binding.recycleMarketList.itemAnimator = DefaultItemAnimator()

        trendMarketsAdaptor = TrendMarketsAdaptor(requireActivity(), marketsList, this)
        marketListAdaptor = MarketsListAdaptor(requireActivity(), marketsList, this)

        binding.recycleTrendMarkets.adapter = trendMarketsAdaptor
        binding.recycleMarketList.adapter = marketListAdaptor

        binding.nestedScrollView.setOnScrollChangeListener (View.OnScrollChangeListener { _, _, scrollY, _, _ ->
            when {
                scrollY == 0 -> {
                    marketViewModel.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.transparent))
                }
                scrollY < 512 -> {
                    marketViewModel.setToolbarColor(Color.argb((scrollY * .5).toInt(), 112, 112, 112))
                }
                else -> {
                    marketViewModel.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.market_header_mask))
                }
            }
        })

        binding.lblAllMarket.setOnClickListener {
            allMarketsBottomSheetFragment.show(requireActivity().supportFragmentManager, ALL_MARKET_TAG)
        }

        getUserInfo()
        listenForViewModelDataChange()

    }

    override fun onMarketItemClick(item: MarketObject) {
        marketViewModel.setMarket(item)
        marketViewModel.setFragment(MarketFragments.EXCHANGE)
    }

    override fun onTrendItemClick(item: MarketObject?) {
        marketViewModel.setMarket(item)
        marketViewModel.setFragment(MarketFragments.EXCHANGE)
    }

    private fun listenForViewModelDataChange() {
        marketViewModel.socketResponse.observe(viewLifecycleOwner, { marketData: String? ->
            marketData?.let{
                if (!JSONObject(marketData).has("error")) {
                    if (JSONObject(marketData).getString("method") == "price.update") {
                        val moneyKey: String? = updateMarketsDictionary(marketData)
                        updateMarketsListPrice(moneyKey)
                        updateAdaptors(moneyKey)
                    }
                }
                if (JSONObject(marketData).has("result") && JSONObject(marketData)["result"] is JSONArray) {
                    val moneyName: String = updateMarketChartInfo(marketData)
                    updateAdaptors(moneyName)
                }
            }
        })

        // listens for api response data
        marketViewModel.apiResponse.observe(viewLifecycleOwner, { responsePair: Pair<String, RequestType> ->
            val response = responsePair.first
            val requestType = responsePair.second

            response?.let {
                when (requestType) {
                    RequestType.USER -> {
                        val hashResponse = APIResponseParser().parseResponse(requestType, response)
                        binding.lblName.text = hashResponse["name"].toString() + " " + hashResponse["surname"].toString()
                    }
                    RequestType.WALLET_BALANCE -> { setNetWorth( parseBalanceResponse(response)) }
                    RequestType.MARKET_LIST -> {
                        marketsList?.clear()

                        marketsList = parseMarketList(response)
                        saveMarketsIntoPreferences(marketsList!!)
                        createMarketsDictionary(marketsList!!)
                        fetchMarketsInfo()
                        fetchMarketsChartInfo() // fetches information for chart (kline)
                        binding.lblAllMarket.isClickable = true
                    }
                }
                // dismiss the loading dialog
                marketViewModel.setShowWaitingBar(false)
            }
        })

        marketViewModel.isRequestFailed.observe(viewLifecycleOwner, { failurePair: Pair<Boolean?, RequestType> ->
            when (failurePair.second) {
                RequestType.USER -> getUserInfo()
                RequestType.WALLET_BALANCE -> getUserWalletBalance()
                RequestType.MARKET_LIST -> getMarketsList()
            }
        })
    }

    private fun getUserInfo() {
        val url = RequestUrls().getUrl(RequestType.USER)
        val request = Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(requireActivity(), "token"))
            .post(EMPTY_REQUEST)
            .tag(USER_TAG)
            .build()
        marketViewModel.setApiRequest(request, RequestType.USER)
    }
    private fun getUserWalletBalance() {
        val url = RequestUrls().getUrl(RequestType.WALLET_BALANCE)
        val request = Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(requireActivity(), "token"))
            .post(EMPTY_REQUEST)
            .tag(BALANCE_TAG)
            .build()
        marketViewModel.setApiRequest(request, RequestType.WALLET_BALANCE)
    }
    private fun getMarketsList() {
        val url = RequestUrls().getUrl(RequestType.MARKET_LIST)
        val request = Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(requireActivity(), "token"))
            .post(EMPTY_REQUEST)
            .tag(MARKET_TAG)
            .build()
        marketViewModel.setApiRequest(request, RequestType.MARKET_LIST)
    }

    private fun parseBalanceResponse(response: String) : String? {
        return JSONObject(response).getJSONObject("balances").getString("totalBalance")
    }

    private fun setNetWorth(netWorth: String?) {
        netWorth?.let {
            binding.lblBalanceValue.text = utilities.reduceDecimal(it, 2) + " ₺"
        }
    }

    @Throws(JSONException::class)
    private fun parseMarketList(response: String): ArrayList<MarketObject>? {
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

    // TODO MAIN ACTIVITY SHOULD DO IT
    private fun saveMarketsIntoPreferences(list: java.util.ArrayList<MarketObject>) {
        // getting data from gson and storing it in a string.
        val stringJSON: String = gson.toJson(list)
        utilities.saveInSharedPreferences(requireActivity(), "market_dict", stringJSON)
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

    private fun createMarketsDictionary(list: ArrayList<MarketObject>) {
        marketsDictionary.clear()
        for (market in list) {
            marketsDictionary[market.name] = market
        }
    }

    @Throws(JSONException::class)
    private fun fetchMarketsInfo() {
        val marketsJSONArray = JSONArray()
        for (market in marketsList!!) { marketsJSONArray.put(market.name) }

        val jsonRequest = JSONObject()
            .put("id", 1)
            .put("method", "price.subscribe")
            .put("params", marketsJSONArray)

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
            if (index != -1) {
                marketsList!![index].price = marketsDictionary[moneyName]!!.price
            }
        }
    }

    private fun updateAdaptors(moneyName: String?) {
        // if the arraylist in the adaptor is empty the whole list will be updated
        moneyName?.let { name ->
            with(trendMarketsAdaptor) {
                when (itemCount) {
                    0 -> updateDatabase(marketsList)
                    else -> notifyItemChanged(ArrayList(marketsDictionary.keys).indexOf(name))
                }
            }
            with(marketListAdaptor) {
                when(itemCount) {
                    0 -> updateDatabase(marketsList)
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
            with(resultArray.getJSONArray(INDEX)) {
                // average of close and open in the period
                val average = (this[1].toString().toFloat() + this[2].toString().toFloat()) / 2
                lineChartDataPoints.add(Entry(INDEX.toFloat(), average))
            }
        }
        val moneyName = resultArray.getJSONArray(0)[7].toString()
        marketsList?.get(ArrayList(marketsDictionary.keys).indexOf(moneyName))?.chartData = lineChartDataPoints
        return moneyName
    }

    override fun onResume() {
        super.onResume()

        getUserWalletBalance()
        marketsList?.clear()
        marketsList = loadMarketsFromPreferences()

        if (marketsList.isNullOrEmpty()) {
            getMarketsList()
        } else {
            createMarketsDictionary(marketsList!!)
            fetchMarketsInfo()
            fetchMarketsChartInfo()
        }
    }

    override fun onPause() {
        if(allMarketsBottomSheetFragment.isVisible) {
            allMarketsBottomSheetFragment.dismiss()
        }
        super.onPause()
    }
}