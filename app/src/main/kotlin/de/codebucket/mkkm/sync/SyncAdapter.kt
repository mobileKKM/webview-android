package de.codebucket.mkkm.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import de.codebucket.mkkm.MobileKKM
import de.codebucket.mkkm.R
import de.codebucket.mkkm.activity.SplashActivity

class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {

    object Const {
        const val TAG = "SyncAdapter"
        const val NOTIFICATION_ID = 1000
    }

    private var mAccountManager: AccountManager = AccountManager.get(context)

    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        Log.d(Const.TAG, "Starting sync...");

        val accounts = mAccountManager.getAccountsByType(AuthenticatorService.ACCOUNT_TYPE)
        if (accounts.isEmpty()) {
            return
        }

        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val builder = NotificationCompat.Builder(context, MobileKKM.Const.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_kkm)
            .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
            .setContentTitle(context.getString(R.string.update_notification_title))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.update_notification_message)))
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(Const.NOTIFICATION_ID, builder.build())
        }

        accounts.forEach { account ->
            if (Build.VERSION.SDK_INT >= 22) {
                mAccountManager.removeAccount(account, null, null, null)
            } else {
                // noinspection deprecation
                mAccountManager.removeAccount(account, null, null)
            }
        }

        Log.d(Const.TAG, "Sync finished!");
    }
}
