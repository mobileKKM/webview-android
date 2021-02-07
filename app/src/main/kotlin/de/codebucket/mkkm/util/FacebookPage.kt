package de.codebucket.mkkm.util

import android.content.Context
import android.content.pm.PackageManager

class FacebookPage {
    companion object {
        val FACEBOOK_URL = "https://www.facebook.com/getmobilekkm"
        val FACEBOOK_PAGE_ID = "496708800811072"

        fun getFacebookPageUrl(context: Context): String {
            val packageManager = context.packageManager

            try {
                val packageInfo = packageManager.getPackageInfo("com.facebook.katana", PackageManager.GET_ACTIVITIES)
                if (packageInfo.applicationInfo.enabled) {
                    // app is installed and enabled
                    return "fb://page/$FACEBOOK_PAGE_ID"
                }
            } catch (ignored: PackageManager.NameNotFoundException) {}

            // return normal web url if app is not installed or disabled
            return FACEBOOK_URL
        }
    }
}
