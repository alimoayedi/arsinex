package com.arsinex.com.transactionsPackage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinex.com.marketPackage.MarketViewModel
import com.arsinex.com.R
import com.arsinex.com.databinding.FragmentTransactionsBinding
import com.arsinex.com.enums.RequestType
import com.arsinex.com.objectsPackage.RequestUrls
import com.arsinex.com.utilitiesPackage.CustomSpinnerAdapter
import com.arsinex.com.utilitiesPackage.Utilities
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TransactionsFragment : Fragment() {

    private companion object FragmentConstants {
        const val JSON = "application/json; charset=utf-8"

        val ASSETS_TYPE = listOf<String>("Crypto", "Currency")
        val TRANSACTION_TYPE = listOf<String>("All", "Deposit", "Withdraw")
        val TIME_INTERVALS = listOf<String>("All", "Yesterday", "7 Days", "30 Days")

        const val GET_DEPOSIT_HISTORY_TAG = "deposit_history"
        const val GET_WITHDRAW_HISTORY_TAG = "deposit_history"
    }

    private val marketViewModel: MarketViewModel by activityViewModels()

    private lateinit var binding: FragmentTransactionsBinding

    private lateinit var assetTypeAdaptor: CustomSpinnerAdapter<String>
    private lateinit var transactionTypeAdaptor: CustomSpinnerAdapter<String>
    private lateinit var transactionTimeAdaptor: CustomSpinnerAdapter<String>
    private lateinit var assetAdaptor: CustomSpinnerAdapter<String>
    private lateinit var statusAdaptor: CustomSpinnerAdapter<String>

//    private var assetTypeList: ArrayList<AssetObject> = ArrayList()
//    private var transactionTypeList: ArrayList<AssetObject> = ArrayList()
//    private val transactionTimeList: ArrayList<NetworkObject> = ArrayList()
//    private var assetList: ArrayList<BankObject> = ArrayList()
//    private var statusList = ArrayList()

    private var assetTypeSpinnerList = ArrayList<String>()
    private var transactionTypeSpinnerList = ArrayList<String>()
    private var transactionTimeSpinnerList = ArrayList<String>()
    private var assetSpinnerList = ArrayList<String>()
    private var statusSpinnerList = ArrayList<String>()

    private val utilities = Utilities()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        val transactionView: View = binding.root

        marketViewModel.setToolbarColor(ContextCompat.getColor(transactionView.context, R.color.transparent))

        return transactionView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assetTypeAdaptor = CustomSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_item, assetTypeSpinnerList)
        assetAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spAssetType.adapter = assetAdaptor

        transactionTypeAdaptor = CustomSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_item, transactionTypeSpinnerList)
        transactionTypeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTransactionType.adapter = transactionTypeAdaptor

        transactionTimeAdaptor = CustomSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_item, transactionTimeSpinnerList)
        transactionTimeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTransactionTime.adapter = transactionTimeAdaptor

        assetAdaptor = CustomSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_item, assetSpinnerList)
        assetAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spAsset.adapter = assetAdaptor

        statusAdaptor = CustomSpinnerAdapter(requireContext(), android.R.layout.simple_spinner_item, statusSpinnerList)
        statusAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spStatus.adapter = statusAdaptor

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rwTransactionHistory.layoutManager = layoutManager
        binding.rwTransactionHistory.itemAnimator = DefaultItemAnimator()

        loadAssetTypeSpinner()
        loadTransactionTypeSpinner()
        loadTimeSpinner()

        binding.spAsset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                getCryptoDepositHistory() // TODO asset id is needed as input.
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }

        }


        marketViewModel.setShowWaitingBar(true)

    }

    private fun loadAssetTypeSpinner() {
        assetTypeSpinnerList = ASSETS_TYPE as ArrayList<String>
        binding.spAssetType.setSelection(0)
        assetTypeAdaptor.notifyDataSetChanged()
    }

    private fun loadTransactionTypeSpinner() {
        transactionTypeSpinnerList = TRANSACTION_TYPE as ArrayList<String>
        binding.spTransactionType.setSelection(0)
        transactionTypeAdaptor.notifyDataSetChanged()
    }

    private fun loadTimeSpinner() {
        transactionTimeSpinnerList = TIME_INTERVALS as ArrayList<String>
        binding.spTransactionTime.setSelection(0)
        transactionTimeAdaptor.notifyDataSetChanged()
    }

    private fun getCryptoDepositHistory(assetID: String ) {
        val url: String = RequestUrls.GET_CRYPTO_DEPOSIT_HISTORY
        val jsonRequest: JSONObject = JSONObject().put("asset_id", assetID)
        val requestBody: RequestBody = jsonRequest.toString().toRequestBody(JSON.toMediaTypeOrNull())
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(requireActivity(), "token"))
            .post(requestBody)
            .tag(GET_DEPOSIT_HISTORY_TAG)
            .build()
        marketViewModel.setApiRequest(request, RequestType.GET_CRYPTO_DEPOSIT_HISTORY)
    }

    private fun getWithdrawHistory(assetID: String) {
        val url: String = RequestUrls.GET_WITHDRAW_HISTORY
        val jsonRequest: JSONObject = JSONObject().put("asset_id", assetID)
        val requestBody: RequestBody = jsonRequest.toString().toRequestBody(JSON.toMediaTypeOrNull())
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(requireActivity(), "token"))
            .post(requestBody)
            .tag(GET_WITHDRAW_HISTORY_TAG)
            .build()
        marketViewModel.setApiRequest(request, RequestType.GET_WITHDRAW_HISTORY)
    }


    private fun listenForComingData() {

    }

}