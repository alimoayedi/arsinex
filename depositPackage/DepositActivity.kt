package com.arsinex.com.depositPackage

import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.arsinex.com.LoginRegister.LoginRegisterActivity
import com.arsinex.com.Objects.AssetObject
import com.arsinex.com.Objects.BankObject
import com.arsinex.com.Objects.NetworkObject
import com.arsinex.com.R
import com.arsinex.com.Utilities.MainActivityUtilities
import com.arsinex.com.Utilities.Utilities
import com.arsinex.com.databinding.ActivityDepositBinding
import com.arsinex.com.databinding.LayoutDepositBinding
import com.arsinex.com.databinding.LayoutDrawerMenuBinding
import com.arsinex.com.objectsPackage.ConnectionSettings
import com.arsinex.com.enumsPackage.RequestTypes
import com.arsinex.com.objectsPackage.RequestUrls
import com.google.android.material.tabs.TabLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.jetbrains.annotations.NotNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

class DepositActivity : AppCompatActivity() {

    private  companion object ActivityConstants {
        const val GET_ASSETS_LIST_TAG = "get_assets"
        const val GET_DEPOSIT_BANK_ACCOUNTS_TAG = "get_accounts"
        const val GET_NETWORKS_LIST_TAG = "get_networks"
        const val GET_WALLET_ADDRESS_TAG = "get_wallet"
        const val QR_SIZE = 650
        const val JSON = "application/json; charset=utf-8"
    }

    private lateinit var binding: ActivityDepositBinding // initialize binding
    private lateinit var viewBinding: LayoutDepositBinding
    private lateinit var drawerLayout: DrawerLayout

    private val client: OkHttpClient = OkHttpClient()

    private lateinit var assetAdaptor: ArrayAdapter<String>
    private lateinit var networkAdaptor: ArrayAdapter<String>

    private var fiatList: ArrayList<AssetObject> = ArrayList()
    private var cryptoList: ArrayList<AssetObject> = ArrayList()
    private val networkList: ArrayList<NetworkObject> = ArrayList()
    private var bankList: ArrayList<BankObject> = ArrayList()

    private var userWalletDesc: String? = null

    private val assetsListSpinner = ArrayList<String>()
    private val networksListSpinner = ArrayList<String>()

    private val utilities = Utilities()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositBinding.inflate(layoutInflater)
        viewBinding = binding.includeLayout // layout components
        drawerLayout = binding.drawerLayout // drawer menu components

        binding.lyToolbar.btnMenu.setOnClickListener { clickMenu() }

        setDrawerMenu()

        setConnectionTimeout()

        viewBinding.lblWalletAddress.setAutoSizeTextTypeUniformWithConfiguration(10, 18, 1, TypedValue.COMPLEX_UNIT_SP)
        TransitionManager.beginDelayedTransition(viewBinding.lyMain, AutoTransition())

        setupAssetAdaptor()
        assetAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spAssets.adapter = assetAdaptor

        setupNetworkAdaptor() // set the view of spinner dropdown
        networkAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.spNetwork.adapter = networkAdaptor

        viewBinding.cardBankAccount.visibility = View.GONE
        viewBinding.lyWalletAddress.visibility = View.GONE
        viewBinding.btnCopyAddress.visibility = View.GONE
        viewBinding.btnShareAddress.visibility = View.GONE

        fetchAssetsList()

        viewBinding.tabLyCurrency.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateSpinners(RequestTypes.GET_ASSETS_LIST)
                resetNetworkSpinner()
                resetUI()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        viewBinding.spAssets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when {
                    position == 0 -> {
                        networkAdaptor.clear()
                        resetUI()
                    }
                    viewBinding.tabLyCurrency.selectedTabPosition == 0 -> {
                        val assetId = fiatList[position - 1].assetId
                        fetchBanksList(assetId)
                    }
                    viewBinding.tabLyCurrency.selectedTabPosition == 1 -> {
                        val assetId = cryptoList[position - 1].assetId
                        fetchNetworksList(assetId)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
        viewBinding.spNetwork.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when {
                    position == 0 -> {
                        resetUI()
                    }
                    viewBinding.tabLyCurrency.selectedTabPosition == 0 -> {
                        displaySelectedBankDetails(bankList[position - 1])
                    }
                    viewBinding.tabLyCurrency.selectedTabPosition == 1 -> {
                        fetchWalletAddress(networkList[position - 1].network_id)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewBinding.imgCopyAccName.setOnClickListener {
            val accName: String = viewBinding.lblAccountName.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.account_name), accName)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, resources.getString(R.string.acc_name_copied), Toast.LENGTH_SHORT).show()
        }

        viewBinding.imgCopyAccNum.setOnClickListener {
            val accName: String = viewBinding.lblAccountNumber.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.account_number), accName)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, resources.getString(R.string.acc_num_copied), Toast.LENGTH_SHORT).show()
        }

        viewBinding.imgCopyIban.setOnClickListener {
            val accName: String = viewBinding.lblIban.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.iban_uban), accName)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, resources.getString(R.string.iban_copied), Toast.LENGTH_SHORT).show()
        }

        viewBinding.imgCopyDesc.setOnClickListener {
            val accName: String = viewBinding.lblAccountName.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.description), accName)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, resources.getString(R.string.desc_copied), Toast.LENGTH_SHORT).show()
        }

        viewBinding.btnCopyAddress.setOnClickListener {
            var address: String? = null
            when (viewBinding.tabLyCurrency.selectedTabPosition) {
                0 -> {
                    address = getAccountInfo()
                    Toast.makeText(this, resources.getString(R.string.acc_info_copied), Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    address = viewBinding.lblWalletAddress.text.toString()
                    Toast.makeText(this, resources.getString(R.string.wallet_address_copied), Toast.LENGTH_SHORT).show()
                }
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.wallet_address), address)
            clipboard.setPrimaryClip(clip)
        }

        viewBinding.btnShareAddress.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            when (viewBinding.tabLyCurrency.selectedTabPosition) {
                0 -> {
                    val title = resources.getString(R.string.acc_info)
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getAccountInfo())
                    startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share_acc_info)))
                }
                1 -> {
                    val title = resources.getString(R.string.wallet_address)
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, viewBinding.lblWalletAddress.text.toString())
                    startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share_address_with)))
                }
            }
        }

        setContentView(binding.root)
    }

    private fun setDrawerMenu() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val drawerMenu: LayoutDrawerMenuBinding = binding.drawerMenu

        drawerMenu.lyMarket.setOnClickListener { MainActivityUtilities.ClickMarket(this) }
        drawerMenu.lyProfile.setOnClickListener { MainActivityUtilities.ClickProfile(this) }
        drawerMenu.lyWithdraw.setOnClickListener { MainActivityUtilities.ClickWithdraw(this) }
        drawerMenu.lyDeposit.setOnClickListener { clickDeposit() }
        drawerMenu.lyDiscover.setOnClickListener { MainActivityUtilities.ClickDiscover(this) }
        drawerMenu.lyAboutUs.setOnClickListener { MainActivityUtilities.ClickAboutUs(this) }
        drawerMenu.lyContactUs.setOnClickListener { MainActivityUtilities.ClickContactUs(this) }
        drawerMenu.lySupport.setOnClickListener { MainActivityUtilities.ClickSupport(this) }
        drawerMenu.lyLogout.setOnClickListener { MainActivityUtilities.ClickLogout(this) }
    }

    private fun setupAssetAdaptor() {
        // modified instance of ArrayAdapter created
        assetAdaptor = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, assetsListSpinner) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup, ): View {
                val view = super.getView(position, convertView, parent)

                (view as? TextView)?.gravity = Gravity.START
                (view as? TextView)?.textSize = resources.getDimension(R.dimen.base_font_size) / resources.displayMetrics.density
                (view as? TextView)?.setTextColor(resources.getColor(R.color.mainText, null))

                when (position) {
                    0 -> (view as? TextView)?.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.hintColor
                        )
                    )
                    else -> (view as? TextView)?.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.mainText
                        )
                    )
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup, ): View {
                val view = super.getDropDownView(position, convertView, parent)

                (view as? TextView)?.textSize = context.resources.getDimension(R.dimen.base_font_size) / resources.displayMetrics.density
                (view as? TextView)?.setPadding(
                    (resources.getDimension(R.dimen.padding_left) / resources.displayMetrics.density).toInt(),
                    (resources.getDimension(R.dimen.padding_up) / resources.displayMetrics.density).toInt(),
                    (resources.getDimension(R.dimen.padding_right) / resources.displayMetrics.density).toInt(),
                    (resources.getDimension(R.dimen.padding_bottom) / resources.displayMetrics.density).toInt()
                )
                return view
            }
        }
    }
    private fun setupNetworkAdaptor() {
        networkAdaptor = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, networksListSpinner) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)

                (view as? TextView)?.gravity = Gravity.START
                (view as? TextView)?.textSize = resources.getDimension(R.dimen.base_font_size) / resources.displayMetrics.density
                when (position) {
                    0 -> (view as? TextView)?.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.hintColor
                        )
                    )
                    else -> (view as? TextView)?.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.mainText
                        )
                    )
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup, ): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as? TextView)?.textSize =
                    context.resources.getDimension(R.dimen.base_font_size) / resources.displayMetrics.density
                (view as? TextView)?.setPadding(
                    (resources.getDimension(R.dimen.padding_left) / resources.displayMetrics.density).toInt(),
                    (resources.getDimension(R.dimen.padding_up) / resources.displayMetrics.density).toInt(),
                    (resources.getDimension(R.dimen.padding_right) / resources.displayMetrics.density).toInt(),
                    (resources.getDimension(R.dimen.padding_bottom) / resources.displayMetrics.density).toInt()
                )
                return view
            }
        }
    }

    private fun fetchAssetsList() {
        val url = RequestUrls.GET_ASSETS_LIST
        val request: Request = Request.Builder().url(url).addHeader("Authorization", utilities.getDecryptedSharedPreferences(this, "token")).post(
            EMPTY_REQUEST).tag(ActivityConstants.GET_ASSETS_LIST_TAG).build()
        makeApiRequest(RequestTypes.GET_ASSETS_LIST, request)
    }
    private fun fetchBanksList(asset_id: String) {
        val url = RequestUrls.GET_DEPOSIT_BANK_ACCOUNTS
        val jsonRequest: JSONObject = JSONObject().put("asset_id", asset_id)

        val requestBody: RequestBody = jsonRequest.toString().toRequestBody(JSON.toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(this, "token"))
            .post(requestBody)
            .tag(ActivityConstants.GET_DEPOSIT_BANK_ACCOUNTS_TAG)
            .build()
        makeApiRequest(RequestTypes.GET_DEPOSIT_BANK_ACCOUNTS, request)
    }
    private fun fetchNetworksList(asset_id: String) {
        val url = RequestUrls.GET_NETWORKS_LIST
        val jsonRequest: JSONObject = JSONObject().put("asset_id", asset_id)

        val requestBody = jsonRequest.toString().toRequestBody(JSON.toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(this, "token"))
            .post(requestBody)
            .tag(ActivityConstants.GET_NETWORKS_LIST_TAG)
            .build()
        makeApiRequest(RequestTypes.GET_NETWORKS_LIST, request)
    }
    private fun fetchWalletAddress(network_id: String) {
        val url = RequestUrls.GET_WALLET_ADDRESS
        val jsonRequest: JSONObject = JSONObject().put("asset_id", network_id)

        val requestBody = jsonRequest.toString().toRequestBody(JSON.toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", utilities.getDecryptedSharedPreferences(this, "token"))
            .post(requestBody)
            .tag(ActivityConstants.GET_WALLET_ADDRESS_TAG)
            .build()
        makeApiRequest(RequestTypes.GET_WALLET_ADDRESS, request)
    }

    private fun setConnectionTimeout() {
        client.newBuilder()
            .connectTimeout(ConnectionSettings.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(ConnectionSettings.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(ConnectionSettings.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun makeApiRequest(requestType: RequestTypes, @NotNull request: Request) {
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // TODO
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code
                val responseBody = response.body?.string()
                response.close()
                if (!isFinishing) {
                    runOnUiThread {
                        when (responseCode) {
                            HttpsURLConnection.HTTP_OK -> {
                                try {
                                    responseBody?.let { handleSuccessResponse(requestType, it) }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                            else -> {
                                showErrorMessage(responseCode)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun handleSuccessResponse(requestType: RequestTypes, response: String) {
        when (requestType) {
            RequestTypes.GET_ASSETS_LIST,
            RequestTypes.GET_DEPOSIT_BANK_ACCOUNTS,
            RequestTypes.GET_NETWORKS_LIST,
            -> {
                parseResponse(requestType, response)
                updateSpinners(requestType)
            }
            RequestTypes.GET_WALLET_ADDRESS -> {
                displayWalletDetails(response)
            }
            else -> {
                throw IllegalStateException("Error in request type. Not a defined type.")
            }
        }
    }

    @Throws(JSONException::class)
    private fun parseResponse(requestType: RequestTypes, response: String) {
        var jsonArray: JSONArray?
        when (requestType) {
            RequestTypes.GET_ASSETS_LIST -> {
                fiatList.clear()
                jsonArray = JSONObject(response).getJSONArray("fiat_assets")
                for (INDEX in 0 until jsonArray.length()) {
                    val entry = jsonArray.getJSONObject(INDEX)
                    fiatList.add(AssetObject(
                        entry.getString("id"),
                        entry.getString("title"),
                        entry.getString("symbol"),
                        entry.getString("asset_type"),
                        entry.getString("mobile_logo")
                    ))
                }

                cryptoList.clear()
                jsonArray = JSONObject(response).getJSONArray("crypto_assets")
                for (INDEX in 0 until jsonArray.length()) {
                    val entry = jsonArray.getJSONObject(INDEX)
                    cryptoList.add(AssetObject(
                        entry.getString("id"),
                        entry.getString("title"),
                        entry.getString("symbol"),
                        entry.getString("asset_type"),
                        entry.getString("mobile_logo")
                    ))
                }
            }
            RequestTypes.GET_DEPOSIT_BANK_ACCOUNTS -> {
                bankList.clear()
                jsonArray = JSONObject(response).getJSONArray("banks")
                for (INDEX in 0 until jsonArray.length()) {
                    val entry = jsonArray.getJSONObject(INDEX)
                    val bankObject = BankObject(
                        entry.getJSONObject("bank").getString("id"),
                        entry.getJSONObject("bank").getString("title"),
                        entry.getJSONObject("bank").getString("type"),
                        entry.getJSONObject("bank").getString("working_hours"),
                        entry.getJSONObject("bank").getString("logo")
                    )
                    bankObject.accountName = entry.getString("account_name")
                    bankObject.accountIban = entry.getString("account_iban")
                    bankObject.accountNumber = entry.getString("account_number")
                    bankList.add(bankObject)
                }
                userWalletDesc =
                    JSONObject(response).getJSONObject("user_wallets").getString("desc")
            }
            RequestTypes.GET_NETWORKS_LIST -> {
                networkList.clear()
                jsonArray = JSONObject(response).getJSONArray("tokens")
                for (INDEX in 0 until jsonArray.length()) {
                    val entry = jsonArray.getJSONObject(INDEX)
                    networkList.add(NetworkObject(
                        entry.getString("asset_id"),
                        entry.getString("network_id"),
                        entry.getString("network_name"),
                        entry.getString("contract_address"),
                        entry.getString("comission")
                    ))
                }
            }
            else -> {
                throw IllegalStateException("Error in request type. Not a defined type.")
            }
        }
    }

    private fun updateSpinners(requestType: RequestTypes?) {
        when (requestType) {
            RequestTypes.GET_ASSETS_LIST -> if (viewBinding.tabLyCurrency.selectedTabPosition == 0) {
                setupCurrencySpinner()
            } else {
                setupCryptoSpinner()
            }
            RequestTypes.GET_NETWORKS_LIST -> setupNetworkSpinner()
            RequestTypes.GET_DEPOSIT_BANK_ACCOUNTS -> setupBankSpinner()
            else -> {
                throw IllegalStateException("Error in request type. Not a defined type.")
            }
        }
    }

    private fun setupCurrencySpinner() {
        assetsListSpinner.clear()
        assetsListSpinner.add(resources.getString(R.string.choose_currency))
        for (assetObject in fiatList) {
            assetsListSpinner.add(assetObject.assetTitle)
        }
        viewBinding.spAssets.setSelection(0)
        assetAdaptor.notifyDataSetChanged()
    }
    private fun setupCryptoSpinner() {
        assetsListSpinner.clear()
        assetsListSpinner.add(resources.getString(R.string.choose_crypto))
        for (assetObject in cryptoList) {
            assetsListSpinner.add(assetObject.assetTitle)
        }
        viewBinding.spAssets.setSelection(0)
        assetAdaptor.notifyDataSetChanged()
    }
    private fun setupBankSpinner() {
        networksListSpinner.clear()
        networksListSpinner.add(resources.getString(R.string.choose_bank))
        for (bankObject in bankList) {
            networksListSpinner.add(bankObject.title)
        }
        viewBinding.spNetwork.setSelection(0)
        networkAdaptor.notifyDataSetChanged()
    }
    private fun setupNetworkSpinner() {
        networksListSpinner.clear()
        networksListSpinner.add(resources.getString(R.string.choose_network))
        for (networkObject in networkList) {
            networksListSpinner.add(networkObject.network_name)
        }
        viewBinding.spNetwork.setSelection(0)
        networkAdaptor.notifyDataSetChanged()
    }

    private fun displaySelectedBankDetails(selectedBank: BankObject) {
        // set text of bank info card
        viewBinding.lblBankName.text = selectedBank.title
        viewBinding.lblAccountName.text = selectedBank.accountName
        viewBinding.lblAccountNumber.text = selectedBank.accountNumber
        viewBinding.lblIban.text = selectedBank.accountIban
        viewBinding.lblDesc.text = userWalletDesc

        // make bank account card and buttons visible
        viewBinding.cardBankAccount.visibility = View.VISIBLE
        viewBinding.btnCopyAddress.visibility = View.VISIBLE
        viewBinding.btnShareAddress.visibility = View.VISIBLE
    }

    @Throws(JSONException::class)
    private fun displayWalletDetails(response: String) {
        val walletAddress = JSONObject(response).getJSONObject("wallet").getString("address")
        viewBinding.lblWalletAddress.text = walletAddress
        setQRCode(walletAddress)
        viewBinding.lyWalletAddress.visibility = View.VISIBLE
        viewBinding.btnCopyAddress.visibility = View.VISIBLE
        viewBinding.btnShareAddress.visibility = View.VISIBLE
    }

    private fun setQRCode(wallet_address: String?) {
        val sharedPreferences: SharedPreferences? = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences?.getBoolean("night_mode", false)
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(wallet_address,
                BarcodeFormat.QR_CODE,
                ActivityConstants.QR_SIZE,
                ActivityConstants.QR_SIZE)
            val bitmap = Bitmap.createBitmap(ActivityConstants.QR_SIZE,
                ActivityConstants.QR_SIZE,
                Bitmap.Config.RGBA_F16)
            for (x in 0 until ActivityConstants.QR_SIZE) {
                for (y in 0 until ActivityConstants.QR_SIZE) {
                    if (nightMode == true) {
                        bitmap.setPixel(x,
                            y,
                            if (bitMatrix[x, y]) Color.WHITE else ContextCompat.getColor(this,
                                R.color.main_BG))
                    } else {
                        bitmap.setPixel(x,
                            y,
                            if (bitMatrix[x, y]) Color.BLACK else ContextCompat.getColor(this,
                                R.color.main_BG))
                    }
                }
            }
            viewBinding.imgQRCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetNetworkSpinner() {
        networkAdaptor.clear()
    }

    private fun resetUI() {
        viewBinding.cardBankAccount.visibility = View.GONE
        viewBinding.lyWalletAddress.visibility = View.GONE
        viewBinding.btnCopyAddress.visibility = View.GONE
        viewBinding.btnShareAddress.visibility = View.GONE
        when (viewBinding.tabLyCurrency.selectedTabPosition) {
            0 -> {
                viewBinding.btnCopyAddress.text = resources.getString(R.string.copy_acc_info)
                viewBinding.btnShareAddress.text = resources.getString(R.string.share_acc_info)
            }
            else -> {
                viewBinding.btnCopyAddress.text = resources.getString(R.string.copy_address)
                viewBinding.btnShareAddress.text = resources.getString(R.string.share_address)
            }
        }
    }

    private fun showErrorMessage(http_code: Int) {
        // Create an alert builder
        val builder = AlertDialog.Builder(this)

        // set the custom layout
        val dialogView: View = layoutInflater.inflate(R.layout.layout_alert_dialog, null)
        builder.setView(dialogView)

        val dialog = builder.create() // create alert dialog
        val lblMsgHeader: TextView = dialogView.findViewById(R.id.lblMsgHeader)
        val lblMsg: TextView = dialogView.findViewById(R.id.lblMsg)
        val btnNegative: Button = dialogView.findViewById(R.id.btnNegative)
        val btnPositive: Button = dialogView.findViewById(R.id.btnPositive)

        btnNegative.visibility = View.GONE // No need for this button here!
        btnPositive.text = getString(R.string.ok)

        when (http_code) {
            HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                lblMsgHeader.text = resources.getString(R.string.sessionTimeOut)
                lblMsg.setText(R.string.session_timeout_msg)
            }
            else -> {
                lblMsgHeader.text = resources.getString(R.string.error)
                lblMsg.text = http_code.toString()
            }
        }

        btnPositive.setOnClickListener {
            dialog.dismiss()
            when (http_code) {
                HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                    val loginActivity = Intent(this, LoginRegisterActivity::class.java)
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(loginActivity)
                    finishAffinity()
                }
            }
        }
        // show dialog
        dialog.show()
    }

    private fun getAccountInfo(): String {
        return (viewBinding.lblBankName.text.toString()
                + "\n" + resources.getString(R.string.account_name) + " " + viewBinding.lblAccountName.text.toString()
                + "\n" + resources.getString(R.string.account_number) + " " + viewBinding.lblAccountNumber.text.toString()
                + "\n" + resources.getString(R.string.iban_uban) + " " + viewBinding.lblIban.text.toString()
                + "\n" + resources.getString(R.string.description) + ":" + viewBinding.lblDesc.text.toString())
    }

    private fun clickMenu() {
        // open drawer
        MainActivityUtilities.openDrawer(this, drawerLayout)
    }

    private fun clickDeposit() {
        // closes menu
        MainActivityUtilities.closeDrawer(drawerLayout)
    }

    // hide keyboard on opening drawer menu
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken,0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPause() {
        MainActivityUtilities.closeDrawer(drawerLayout)
        super.onPause()
    }
}