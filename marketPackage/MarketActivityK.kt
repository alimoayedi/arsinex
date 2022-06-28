package com.arsinex.com.marketPackage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.arsinex.com.APIResponseParser
import com.arsinex.com.Balance.BalanceFragment
import com.arsinex.com.ConnectionSettings
import com.arsinex.com.Exchange.ExchangeFragment
import com.arsinex.com.LoginRegister.LoginRegisterActivity
import com.arsinex.com.Objects.MarketObject
import com.arsinex.com.Predicition.PredictionFragment
import com.arsinex.com.R
import com.arsinex.com.Utilities.MainActivityUtilities
import com.arsinex.com.WebSocketListener
import com.arsinex.com.databinding.ActivityMarketBinding
import com.arsinex.com.databinding.LayoutDrawerMenuBinding
import com.arsinex.com.enums.MarketFragments
import com.arsinex.com.enums.RequestType
import com.arsinex.com.firebaseNotification.FirebaseNotification
import com.arsinex.com.objectsPackage.RequestUrls
import com.arsinex.com.utilitiesPackage.CustomLoadingDialog
import com.arsinex.com.utilitiesPackage.Utilities
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit

class MarketActivityK : AppCompatActivity() {

    private companion object {
        private const val WEB_SOCKET_TAG = "web_socket_tag"
        private const val MARKET_TAG = "MARKET"
        private const val EXCHANGE_TAG = "EXCHANGE"
        private const val PREDICTION_TAG = "PREDICTION"
        private const val BALANCE_TAG = "BALANCE"
    }

    // view
    private lateinit var binding: ActivityMarketBinding
    private lateinit var drawerLayout: DrawerLayout

    // view model
    private val marketViewModel: MarketViewModel by viewModels()

    private lateinit var loadingBar: CustomLoadingDialog

    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient()
    private val gson = Gson()
    private val utilities = Utilities()

    private val sharedMarket = SharedMarket()

    // fragments
    private val marketFragment = MarketFragmentK()
    private val exchangeFragment = ExchangeFragment()
    private val predictionFragment = PredictionFragment()
    private val balanceFragment = BalanceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMarketBinding.inflate(layoutInflater)
        drawerLayout = binding.drawerLayout
        binding.toolbar.btnMenu.setOnClickListener { openMenu() }
        loadingBar = CustomLoadingDialog(this)

        setupDrawerMenu()
        setConnectionTimeout()

        getSharedData()

        binding.toolbar.btnNotification.setOnClickListener {
            val notificationCenterIntent = Intent(this@MarketActivityK, FirebaseNotification::class.java)
            startActivity(notificationCenterIntent)
        }

        binding.bottomNavBar.setOnNavigationItemSelectedListener { menuItem ->
            run {
                when (menuItem.itemId) {
                    R.id.navExchange -> replaceFragment(MarketFragments.EXCHANGE)
                    R.id.navPrediction -> replaceFragment(MarketFragments.PREDICTION)
                    R.id.navBalance -> replaceFragment(MarketFragments.BALANCE)
                    else -> replaceFragment(MarketFragments.MARKET)
                }
            }
            true
        }

        listenForViewModelChanges()
        setContentView(binding.root)
    }

    private fun setupDrawerMenu() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val drawerMenuView: LayoutDrawerMenuBinding = binding.drawerMenu

        drawerMenuView.lyMarket.setOnClickListener { closeMenu()}
        drawerMenuView.lyProfile.setOnClickListener { MainActivityUtilities.ClickProfile(this@MarketActivityK) }
        drawerMenuView.lyWithdraw.setOnClickListener { MainActivityUtilities.ClickWithdraw(this@MarketActivityK) }
        drawerMenuView.lyDeposit.setOnClickListener { MainActivityUtilities.ClickDeposit(this@MarketActivityK) }
        drawerMenuView.lyDiscover.setOnClickListener { MainActivityUtilities.ClickDiscover(this@MarketActivityK) }
        drawerMenuView.lyAboutUs.setOnClickListener { MainActivityUtilities.ClickAboutUs(this@MarketActivityK) }
        drawerMenuView.lyContactUs.setOnClickListener { MainActivityUtilities.ClickContactUs(this@MarketActivityK) }
        drawerMenuView.lySupport.setOnClickListener { MainActivityUtilities.ClickSupport(this@MarketActivityK) }
        drawerMenuView.lyLogout.setOnClickListener { MainActivityUtilities.ClickLogout(this@MarketActivityK) }

    }
    private fun openMenu() {
        MainActivityUtilities.openDrawer(this, drawerLayout)
    }
    private fun closeMenu() {
        MainActivityUtilities.closeDrawer(drawerLayout)
    }

    private fun setConnectionTimeout() {
        client.newBuilder()
            .connectTimeout(ConnectionSettings().CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(ConnectionSettings().CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(ConnectionSettings().CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun getSharedData() {
        val extraData: Bundle? = intent.extras

        extraData?.let {
            sharedMarket.market = gson.fromJson(extraData.getString("market"), MarketObject::class.java)
            binding.bottomNavBar.selectedItemId = R.id.navExchange
            supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame, exchangeFragment, EXCHANGE_TAG).commit()
        } ?: run {
            binding.bottomNavBar.selectedItemId = R.id.navMarket
            supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame, marketFragment, MARKET_TAG).commit()
        }
    }

    private fun replaceFragment(fragment: MarketFragments) {
        unsubscribeFromSocket()
        when(fragment) {
            MarketFragments.EXCHANGE -> {
                val bundle = Bundle().apply { putString("market", GsonBuilder().create().toJson(sharedMarket.market)) }
                exchangeFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame, exchangeFragment, EXCHANGE_TAG).commit()
            }
            MarketFragments.PREDICTION ->  {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame, predictionFragment, PREDICTION_TAG).commit()
            }
            MarketFragments.BALANCE -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame, balanceFragment, BALANCE_TAG).commit()
            }
            else -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame, marketFragment, MARKET_TAG).commit()
            }
        }
    }

    private fun getActiveFragment(): MarketFragments? {
        when {
            marketFragment != null && marketFragment.isVisible -> {
                return MarketFragments.MARKET
            }
            exchangeFragment != null && exchangeFragment.isVisible -> {
                return MarketFragments.EXCHANGE
            }
            predictionFragment != null && predictionFragment.isVisible -> {
                return MarketFragments.PREDICTION
            }
            balanceFragment != null && balanceFragment.isVisible -> {
                return MarketFragments.BALANCE
            }
            else -> {
                return null
            }
        }
    }

    private fun connectToSocket() {
        val request: Request = Request.Builder().url(RequestUrls.WEB_SOCKET_URL).tag(WEB_SOCKET_TAG).build()
        val webSocketListener: WebSocketListener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, response: String) {
                runOnUiThread {
                    marketViewModel.setSocketResponse(response)
                }
            }
        }
        webSocket = client.newWebSocket(request, webSocketListener)

        // unsubscribes from default updates coming from socket
        sendRequestToSocket(getUnsubscribeRequest("state.unsubscribe").toString())
    }
    private fun sendRequestToSocket(request: String) {
        // show waiting bar
        marketViewModel.setShowWaitingBar(true)
        webSocket.send(request)
    }
    private fun getUnsubscribeRequest(method: String): JSONObject? {
        val paramsArray = JSONArray()
        return JSONObject().put("method", method).put("params", paramsArray).put("id", 1)
    }
    private fun unsubscribeFromSocket() {
        val activeFragment: MarketFragments? = getActiveFragment()
        activeFragment?.let {
            when (activeFragment) {
                MarketFragments.MARKET -> sendRequestToSocket(getUnsubscribeRequest("price.unsubscribe").toString())
                MarketFragments.EXCHANGE -> {
                    sendRequestToSocket(getUnsubscribeRequest("state.unsubscribe").toString())
                    sendRequestToSocket(getUnsubscribeRequest("deals.unsubscribe").toString())
                    sendRequestToSocket(getUnsubscribeRequest("depth.unsubscribe").toString())
                }
                MarketFragments.PREDICTION -> {
                    sendRequestToSocket(getUnsubscribeRequest("price.unsubscribe").toString())
                    sendRequestToSocket(getUnsubscribeRequest("state.unsubscribe").toString())
                }
                else -> throw IllegalArgumentException("Invalid Key, Prediction Card Type")
            }
        }
    }

    private fun listenForViewModelChanges() {

        // listens for status bar color change
        marketViewModel.toolbarColor.observe(this, { color: Int ->
            binding.toolbar.lyToolbar.setBackgroundColor(color)
        })

        marketViewModel.market.observe(this, { market: MarketObject? ->
            sharedMarket.market = market
        })

        marketViewModel.fragment.observe(this, { fragment: MarketFragments ->
            when (fragment) {
                MarketFragments.MARKET -> binding.bottomNavBar.selectedItemId = R.id.navMarket
                MarketFragments.EXCHANGE -> binding.bottomNavBar.selectedItemId = R.id.navExchange
            }
        })

        marketViewModel.showWaitingBar.observe(this, { show: Boolean ->
            if (show) loadingBar.show() else loadingBar.cancel()
            displayShowCase()
        })

        // listens for any request to be send
        marketViewModel.apiRequest.observe(this, {
                requestPair: Pair<Request, RequestType> ->
                val request = requestPair.first
                val requestType = requestPair.second
                sendRequestToAPI(request, requestType)
            })

        marketViewModel.socketRequest.observe(this, { request: String ->
            webSocket.send(request)
        })
    }

    private fun sendRequestToAPI(request: Request, requestType: RequestType) {
        // show waiting bar
        marketViewModel.setShowWaitingBar(true)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!isFinishing) {
                    runOnUiThread {
                        marketViewModel.setShowWaitingBar(false)
                        marketViewModel.setRequestFailed(true, requestType)
                        call.cancel()
                    }
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code
                val responseBody = response.body?.string()
                response.close()
                if (!isFinishing) {
                    runOnUiThread {
                        when (responseCode) {
                            HttpURLConnection.HTTP_OK -> marketViewModel.setApiResponse(responseBody, requestType)
                            HttpURLConnection.HTTP_BAD_REQUEST -> {
                                try {
                                    val hashResponse = APIResponseParser().parseResponse(requestType, responseBody)
                                    marketViewModel.setShowWaitingBar(false)
                                    failureResponse(hashResponse)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                marketViewModel.setShowWaitingBar(false)
                                unauthorizedResponse()
                            }
                            else -> {
                                marketViewModel.setShowWaitingBar(false)
                                failureResponse(null)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun failureResponse(hashResponse: HashMap<Any, Any>?) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.layout_alert_dialog, null)

        builder.setView(dialogView)

        val dialog = builder.create() // create alert dialog
        val lblMsgHeader = dialogView.findViewById<TextView>(R.id.lblMsgHeader)
        val lblMsg = dialogView.findViewById<TextView>(R.id.lblMsg)
        val btnNegative = dialogView.findViewById<Button>(R.id.btnNegative)
        val btnPositive = dialogView.findViewById<Button>(R.id.btnPositive)

        val errorMsg = hashResponse?.getOrDefault("error_msg", "")?.toString() ?: ""

        btnNegative.visibility = View.GONE // No need for this button here!
        lblMsgHeader.text = resources.getString(R.string.server_msg)
        lblMsg.text = errorMsg
        btnPositive.text = resources.getString(R.string.ok)
        dialog.setCancelable(true)
        btnPositive.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun unauthorizedResponse() {
        // Create an alert builder
        val builder = AlertDialog.Builder(this)
        // set the custom layout
        val dialogView = layoutInflater.inflate(R.layout.layout_alert_dialog, null)

        builder.setView(dialogView)

        val dialog = builder.create() // create alert dialog
        val lblMsgHeader = dialogView.findViewById<TextView>(R.id.lblMsgHeader)
        val lblMsg = dialogView.findViewById<TextView>(R.id.lblMsg)
        val btnNegative = dialogView.findViewById<Button>(R.id.btnNegative)
        val btnPositive = dialogView.findViewById<Button>(R.id.btnPositive)

        btnNegative.visibility = View.GONE // No need for this button here!
        btnPositive.text = resources.getString(R.string.ok)
        lblMsgHeader.text = resources.getString(R.string.sessionTimeOut)
        lblMsg.setText(R.string.session_timeout_msg)

        btnPositive.setOnClickListener {
            dialog.dismiss()
            val loginActivity = Intent(this@MarketActivityK, LoginRegisterActivity::class.java)
            loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(loginActivity)
            finishAffinity()
        }

        dialog.setCancelable(false)

        // show dialog
        dialog.show()
    }

    private fun displayShowCase() {
        val preferences = getSharedPreferences("settings", MODE_PRIVATE)
        if (!preferences.contains("showcase_visited")) {
            if (getActiveFragment() == MarketFragments.MARKET) {
                MarketShowcase(this, findViewById<View>(android.R.id.content).rootView).startShowcase()
                val editor = getSharedPreferences("settings", MODE_PRIVATE).edit()
                editor.putBoolean("showcase_visited", true)
                editor.apply()
            }
        }
    }

    private fun askForLogout() {
        val builder = AlertDialog.Builder(this)
        val exitDialog = layoutInflater.inflate(R.layout.layout_alert_dialog, null)

        builder.setView(exitDialog)

        val lblMsgHeader = exitDialog.findViewById<TextView>(R.id.lblMsgHeader)
        val lblMsg = exitDialog.findViewById<TextView>(R.id.lblMsg)
        val btnNegative = exitDialog.findViewById<Button>(R.id.btnNegative)
        val btnPositive = exitDialog.findViewById<Button>(R.id.btnPositive)

        lblMsgHeader.text = resources.getString(R.string.exitQuestion)
        lblMsg.visibility = View.GONE
        btnPositive.text = resources.getString(R.string.yes)
        btnNegative.text = resources.getString(R.string.no)

        val dialog = builder.create()
        btnPositive.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }
        btnNegative.setOnClickListener { dialog.cancel() }
        dialog.show()
    }

    private fun dismissAllDialogs(manager: FragmentManager) {
        val fragments = manager.fragments ?: return
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                fragment.dismissAllowingStateLoss()
            }
            val childFragmentManager = fragment.childFragmentManager
            childFragmentManager?.let { dismissAllDialogs(childFragmentManager) }
        }
    }

    override fun onBackPressed() {
        val activeFragment = getActiveFragment() ?: return
        if (activeFragment == MarketFragments.MARKET) {
            askForLogout()
        } else {
            unsubscribeFromSocket()
            replaceFragment(MarketFragments.MARKET)
        }
    }

    override fun onResume() {
        super.onResume()
        connectToSocket()
    }

    override fun onPause() {
        // Close drawer
        unsubscribeFromSocket()
        MainActivityUtilities.closeDrawer(drawerLayout)
        super.onPause()
    }

    override fun onDestroy() {
        // removes list of markets at the time of leaving market activity
        utilities.removeFromSharedPreferences(this, "market_dict")
        webSocket.close(1000, "destroyed")
        super.onDestroy()
    }

    override fun onStop() {
        dismissAllDialogs(this.supportFragmentManager)
        super.onStop()
    }
}