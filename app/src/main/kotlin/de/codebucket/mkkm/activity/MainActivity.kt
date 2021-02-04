package de.codebucket.mkkm.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.snackbar.Snackbar

import de.codebucket.mkkm.KKMWebViewClient
import de.codebucket.mkkm.MobileKKM
import de.codebucket.mkkm.R
import de.codebucket.mkkm.databinding.ActivityMainBinding

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : AppCompatActivity() {

    object Const {
        const val FILE_CHOOSER_RESULT_CODE = 100
        const val TIME_INTERVAL = 2000
    }

    private lateinit var binding: ActivityMainBinding

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mBrightnessToggled = false
    private var mBackPressed: Long = 0

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener {
            val layout = window.attributes

            if (!mBrightnessToggled) {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            } else {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }

            mBrightnessToggled = !mBrightnessToggled
            window.attributes = layout

            binding.fab.setImageResource(if (mBrightnessToggled) R.drawable.ic_lightbulb_on else R.drawable.ic_lightbulb_off)
        }

        val webview = binding.webview
        webview.webChromeClient = UploadWebChromeClient()
        webview.webViewClient = KKMWebViewClient(this, binding)

        // Allow 3rd party cookies, otherwise remember me won't work
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true)

        // Set webview settings for webapps
        webview.settings.displayZoomControls = false
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.setAppCacheEnabled(true)
        webview.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        if (MobileKKM.isDebug) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // Load the app
        webview.clearCache(true)
        webview.loadUrl("https://m.kkm.krakow.pl")

        val preferences = MobileKKM.preferences
        if (!preferences.getBoolean("tutorial_done", false) || MobileKKM.isDebug) {
            MaterialTapTargetPrompt.Builder(this@MainActivity)
                .setTarget(binding.fab)
                .setPrimaryText(R.string.fab_prompt_title)
                .setSecondaryText(R.string.fab_prompt_message)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener { _: MaterialTapTargetPrompt, state: Int ->
                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                        preferences.edit().putBoolean("tutorial_done", true).apply()
                    }
                }
                .show()
        }
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
            Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show()
            mBackPressed = System.currentTimeMillis()
            return
        }

        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Don't continue if someone tried to call activity without url
        if (intent == null || intent.data == null) {
            return
        }

        val data = intent.data!!

        // Check if it contains both id and result parameters
        if (data.getQueryParameter("id") == null || data.getQueryParameter("result") == null) {
            Snackbar.make(binding.swipe, R.string.no_payment, Snackbar.LENGTH_LONG).show()
            return
        }

        when (data.getQueryParameter("result")) {
            "ok" -> Snackbar.make(binding.swipe, R.string.payment_complete, Snackbar.LENGTH_LONG).show()
            "error" -> Snackbar.make(binding.swipe, R.string.payment_error, Snackbar.LENGTH_LONG).show()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.webview.clearCache(true)
            binding.webview.reload()
        }, 500L)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    inner class UploadWebChromeClient : WebChromeClient() {

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
