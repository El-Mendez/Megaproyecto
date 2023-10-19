package me.mendez.ela.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import me.mendez.ela.model.MaliciousAppClassificator
import me.mendez.ela.notifications.SuspiciousAppChannel
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import javax.inject.Inject

private const val TAG = "ELA_PERMISSIONS_MODEL"

@AndroidEntryPoint
class PermissionCheck : BroadcastReceiver() {
    @Inject
    lateinit var suspiciousAppDao: SuspiciousAppDao

    override fun onReceive(context: Context, intent: Intent?) {
        if (!listOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_PACKAGE_INSTALL,
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                "me.mendez.ela.TEST_APP_INSTALL",
            ).contains(intent?.action)
        ) {
            Log.i(TAG, "unknown intent action: ${intent?.action}")
            return
        }

        runBlocking {
            val currentForbidden = getCurrentSuspiciousApps(context)
            Log.i(TAG, "found forbidden apps: ${currentForbidden.joinToString(", ") { it.packageName }}")

            val oldForbidden = suspiciousAppDao.all()
                .map { it.packageName }

            val newForbidden = currentForbidden
                .filter { !oldForbidden.contains(it.packageName) }
                .map { it.packageName }

            Log.i(TAG, "new forbidden apps: ${newForbidden.joinToString(", ") { it }}")

            suspiciousAppDao.setSuspiciousApps(currentForbidden)

            if (newForbidden.isEmpty()) return@runBlocking

            SuspiciousAppChannel.notify(
                context,
                SuspiciousAppChannel.SUSPICIOUS_APP_ID,
            ) {
                newSuspiciousApp(newForbidden)
            }
        }
    }

    private fun getCurrentSuspiciousApps(context: Context): List<PackageInfo> {
        val model = MaliciousAppClassificator(context)
        model.load()

        val packages = getAllPackages(context)
        val suspicious = packages
            .filter {
                model.predict(it.requestedPermissions)
            }

        model.destroy()
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
}
