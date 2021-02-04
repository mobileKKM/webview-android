package de.codebucket.mkkm

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat

import com.google.android.material.snackbar.Snackbar

import de.codebucket.mkkm.databinding.ActivityMainBinding
import de.codebucket.mkkm.util.TPayPayment

class KKMWebViewClient(var context: Context, var binding: ActivityMainBinding) : WebViewClient() {

    object Const {
        const val TAG = "KKMWebViewClient"
        const val PAYMENT_URL = "https://secure.tpay.com/?id=27659"
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        binding.swipe.isEnabled = true
        binding.swipe.isRefreshing = true
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)

        if (view == null || url == null) {
            return
        }

        if (url.startsWith(Const.PAYMENT_URL)) {
            view.stopLoading()

            val returnUrl = "mobilekkm://payment?id=%s&result=%s"
            val paymentBuilder = TPayPayment.Builder().fromPaymentLink(url)
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

            view.loadUrl("https://m.kkm.krakow.pl/tickets")
        }
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)

        // Ignore error if it isn't our main page
        if (!request!!.isForMainFrame) {
            return
        }

        binding.swipe.isRefreshing = false
        binding.swipe.isEnabled = false

        Snackbar.make(binding.swipe, R.string.error_no_network, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.snackbar_retry) {
                view?.reload()
            }
            .setActionTextColor(Color.YELLOW)
            .show()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        binding.swipe.isRefreshing = false
        binding.swipe.isEnabled = false
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
