package de.codebucket.mkkm

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.google.android.material.snackbar.Snackbar

import de.codebucket.mkkm.util.TPayPayment

class KKMWebViewClient(var context: Context, var swipe: SwipeRefreshLayout) : WebViewClient() {

    private var firstLoad = true
    private var loadSuccess = true

    object Const {
        const val TAG = "KKMWebViewClient"
        const val PAYMENT_HOST = "secure.tpay.com"
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        swipe.isEnabled = true
        swipe.isRefreshing = true

        loadSuccess = true
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)

        // Ignore error if it isn't our main page
        if (!request!!.isForMainFrame) {
            return
        }

        swipe.isRefreshing = false
        swipe.isEnabled = false

        loadSuccess = false

        Snackbar.make(swipe, R.string.error_no_network, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.snackbar_retry) {
                view?.reload()
            }
            .setActionTextColor(Color.YELLOW)
            .show()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        // Show webview after it has loaded first time
        if (view!!.visibility == View.GONE) {
            view.visibility = View.VISIBLE
        }

        swipe.isRefreshing = false
        swipe.isEnabled = false

        if (firstLoad && loadSuccess) {
            firstLoad = false
            Handler(Looper.getMainLooper()).postDelayed({
                Snackbar.make(swipe, R.string.loading_problem, 8500)
                    .setAction(R.string.action_clear_cache) {
                        view.clearCache(true)
                        view.reload()
                    }
                    .setActionTextColor(Color.YELLOW)
                    .show()
            }, 1500L)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        // Ignore request if it isn't our main page
        if (request == null || !request.isForMainFrame) {
            return false
        }

        when (request.url.scheme) {
            "http", "https" ->  {
                if (request.url.host == Const.PAYMENT_HOST) {
                    interceptPaymentUrl(view, request.url)
                    return true
                }
            }
            "mailto" -> {
                val intent = Intent(Intent.ACTION_SENDTO, request.url)
                context.startActivity(intent)
                return true
            }
            "tel" -> {
                val intent = Intent(Intent.ACTION_DIAL, request.url)
                context.startActivity(intent)
                return true
            }
        }

        return super.shouldOverrideUrlLoading(view, request)
    }

    private fun interceptPaymentUrl(view: WebView?, url: Uri?) {
        val returnUrl = "mobilekkm://payment?id=%s&result=%s"
        val paymentBuilder = TPayPayment.Builder().fromPaymentLink(url.toString())
        paymentBuilder.setReturnUrl(String.format(returnUrl, paymentBuilder.crc, "ok"))
        paymentBuilder.setReturnErrorUrl(String.format(returnUrl, paymentBuilder.crc, "error"))
        paymentBuilder.setOnline(1.toString())

        try {
            val intent = buildCustomTabsIntent()
            intent.launchUrl(context, paymentBuilder.build())
        } catch (ex: ActivityNotFoundException) {
            Log.e(Const.TAG, "No browser found!")
            Toast.makeText(context, R.string.no_browser_activity, Toast.LENGTH_SHORT).show()
        }

        view?.loadUrl("https://m.kkm.krakow.pl/tickets")
    }

    private fun buildColorSchemeParams(): CustomTabColorSchemeParams {
        return CustomTabColorSchemeParams.Builder()
            .setNavigationBarColor(Color.BLACK)
            .setToolbarColor(ContextCompat.getColor(context, R.color.toolbar_color))
            .setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.status_bar_color))
            .build()
    }

    private fun buildCustomTabsIntent(): CustomTabsIntent {
        return CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(buildColorSchemeParams())
            .setShowTitle(false)
            .build()
    }
}
