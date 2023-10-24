package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import me.mendez.ela.ml.MaliciousAppClassifier
import me.mendez.ela.notifications.SuspiciousAppChannel
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import javax.inject.Inject

private const val TAG = "ELA_PERMISSIONS_MODEL"

@AndroidEntryPoint
class PermissionCheck : BroadcastReceiver() {
    @Inject
    lateinit var suspiciousAppDao: SuspiciousAppDao

    override fun onReceive(context: Context, intent: Intent?) {
        if (!ACTIONS_TO_LISTEN.contains(intent?.action)) {
            Log.e(TAG, "unknown intent action: ${intent?.action}")
            return
        }

        runBlocking {
            Log.i(TAG, "checking app permissions")
            checkForNewSuspiciousApps(context)
        }
    }

    @Synchronized
    private fun checkForNewSuspiciousApps(context: Context) {
        Thread.sleep(3000)

        val currentForbidden = getCurrentSuspiciousApps(context)

        if (currentForbidden.isEmpty()) {
            Log.d(TAG, "found no suspicious apps")
        } else {
            Log.d(TAG, "found suspicious apps: ${currentForbidden.joinToString(", ") { it.packageName }}")
        }


        val oldForbidden = runBlocking { suspiciousAppDao.all() }
            .map { it.packageName }

        val newForbidden = currentForbidden
            .filter { !oldForbidden.contains(it.packageName) }

        if (newForbidden.isEmpty()) {
            Log.i(TAG, "no new forbidden apps")
        } else {
            Log.i(
                TAG,
                "new forbidden apps (${newForbidden.size}): ${newForbidden.joinToString(", ") { it.packageName }}"
            )
        }

        runBlocking {
            suspiciousAppDao.setSuspiciousApps(currentForbidden)
        }

        if (newForbidden.isEmpty()) return

        SuspiciousAppChannel.notify(
            context,
            SuspiciousAppChannel.SUSPICIOUS_APP_ID,
        ) {
            newSuspiciousApp(
                newForbidden
                    .map {
                        it
                            .applicationInfo
                            .loadLabel(context.packageManager)
                            .toString()
                    }
            )
        }
    }

    private fun getCurrentSuspiciousApps(context: Context): List<PackageInfo> {
        val classifier = MaliciousAppClassifier(context)
        classifier.load()

        val packages = getAllPackages(context)
        val suspicious = packages
            .filter {
                classifier.predict(
                    it.packageName,
                    it.requestedPermissions,
                    0.90f
                ) == MaliciousAppClassifier.Result.MALICIOUS
            }

        classifier.destroy()
        return suspicious
    }

    private fun getAllPackages(context: Context): List<PackageInfo> {
        return context.packageManager
            .getInstalledPackages(PackageManager.GET_PERMISSIONS)
            .filter {
                it.requestedPermissions != null &&
                        it.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                        it.packageName != context.packageName
            }
    }

    private class Helper : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null)
                notify(context)
        }

    }

    companion object {
        private const val NOTIFY_PACKAGE_CHANGE = "me.mendez.ela.APP_INSTALL_NOTIFICATION"
        private val ACTIONS_TO_LISTEN_DYNAMICALLY = arrayOf(
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
        )
        private val ACTIONS_TO_LISTEN: Array<String> = arrayOf(
            *ACTIONS_TO_LISTEN_DYNAMICALLY,
            Intent.ACTION_BOOT_COMPLETED,
            NOTIFY_PACKAGE_CHANGE,
        )

        fun registerListening(context: Context): BroadcastReceiver {
            val broadcastReceiver = Helper()
            ACTIONS_TO_LISTEN_DYNAMICALLY.forEach {
                ContextCompat.registerReceiver(
                    context,
                    broadcastReceiver,
                    IntentFilter(it),
                    ContextCompat.RECEIVER_EXPORTED,
                )
            }
            return broadcastReceiver
        }

        fun unregisterListening(context: Context, broadcastReceiver: BroadcastReceiver) {
            context.unregisterReceiver(broadcastReceiver)
        }

        fun notify(context: Context) {
            Log.d(TAG, "package was changed!")
            val intent = Intent(context, PermissionCheck::class.java)
            intent.action = NOTIFY_PACKAGE_CHANGE
            context.sendBroadcast(intent)
        }
    }
}
