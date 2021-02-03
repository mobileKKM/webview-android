package de.codebucket.mkkm.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.snackbar.Snackbar

import de.codebucket.mkkm.BuildConfig
import de.codebucket.mkkm.R
import de.codebucket.mkkm.databinding.ActivityMainBinding
import me.jfenn.attribouter.Attribouter

class MainActivity : AppCompatActivity() {

    object Const {
        const val FILE_CHOOSER_RESULT_CODE = 100
        const val TIME_INTERVAL = 2000;
    }

    private lateinit var binding: ActivityMainBinding

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mBackPressed: Long = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action",null).show()
        }

        val webview = binding.webview
        webview.webChromeClient = UploadWebChromeClient()
        webview.webViewClient = KKMWebViewClient()

        // Set webview settings for webapps
        webview.settings.displayZoomControls = false
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.setAppCacheEnabled(true)
        webview.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        // Load the app
        webview.loadUrl("https://m.kkm.krakow.pl/auth/login")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != Const.FILE_CHOOSER_RESULT_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        var results: Array<Uri>? = null

        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK && data != null) {
            val dataString: String? = data.dataString
            if (dataString != null) {
                results = arrayOf(Uri.parse(dataString))
            }
        }

        mFilePathCallback!!.onReceiveValue(results)
        mFilePathCallback = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                true
            }
            R.id.action_clear_cache -> {
                binding.webview.clearCache(true)
                binding.webview.reload()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        // Press back to exit twice
        if (mBackPressed + Const.TIME_INTERVAL < System.currentTimeMillis()) {
            Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
            mBackPressed = System.currentTimeMillis();
            return;
        }

        super.onBackPressed();
    }

    private inner class KKMWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.swipe.isEnabled = true
            binding.swipe.isRefreshing = true
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
    }

    private inner class UploadWebChromeClient : WebChromeClient() {

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            if (mFilePathCallback != null) {
                mFilePathCallback!!.onReceiveValue(null)
            }

            mFilePathCallback = filePathCallback
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"

            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_chooser_file)), Const.FILE_CHOOSER_RESULT_CODE)
            return true
        }
    }
}
