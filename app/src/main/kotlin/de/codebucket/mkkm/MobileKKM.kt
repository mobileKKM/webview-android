package de.codebucket.mkkm

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings

import androidx.preference.PreferenceManager

import java.util.UUID

class MobileKKM : Application() {

    object Const {
        const val SALT = "_mkkm"
        const val CHANNEL_ID = "update_notification"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Use Android Device ID as fingerprint
        // mKKM webapp uses fingerprint2.js to generate a fingerprint based on user-agent
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (preferences.getString("fingerprint", null) == null) {
            preferences.edit().putString("fingerprint", fingerprint).apply()
        }

        // Create notification channel on Android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val updateChannel = NotificationChannel(Const.CHANNEL_ID, getString(R.string.update_notification_channel), NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(updateChannel)
    }

    private val fingerprint: String
        @SuppressLint("HardwareIds")
        get() {
            val deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID) + Const.SALT
            return UUID.nameUUIDFromBytes(deviceId.toByteArray()).toString().replace("-".toRegex(), "")
        }

    companion object {
        lateinit var instance: MobileKKM
            private set
        lateinit var preferences: SharedPreferences
            private set
        val isDebug: Boolean
            get() = BuildConfig.DEBUG && BuildConfig.BUILD_TYPE.equals("debug", true)
    }
}
