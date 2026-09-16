package eu.kanade.tachiyomi.data.updater

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.util.system.notificationManager
import eu.kanade.tachiyomi.util.system.updaterEnabled
import exh.log.xLogE
import kotlinx.coroutines.coroutineScope
import tachiyomi.domain.release.service.AppUpdatePolicy
import java.util.concurrent.TimeUnit

class AppUpdateJob(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            if (!updaterEnabled) {
                cancelTask(context)
                return@coroutineScope Result.success()
            }
            AppUpdateChecker().checkForUpdate(context)
            Result.success()
        } catch (e: Exception) {
            xLogE("Unable to check for update", e)
            Result.failure()
        }
    }

    fun NotificationCompat.Builder.update(block: NotificationCompat.Builder.() -> Unit) {
        block()
        context.notificationManager.notify(Notifications.ID_APP_UPDATER, build())
    }

    companion object {
        private const val TAG = "AppUpdateChecker"

        fun setupTask(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Kept in sync with AppUpdatePolicy.CHECK_INTERVAL_HOURS, the throttle
            // GetApplicationRelease uses to decide whether a check actually hits the network.
            // Using the same interval here means a background run is never wasted by landing
            // inside the app's own throttle window. WorkManager doesn't guarantee exact timing
            // (Android/Doze controls background execution), so this is "at least every ~24h
            // when conditions allow", not a real-time guarantee - a flex window is given so the
            // OS can batch this with other work.
            val request = PeriodicWorkRequestBuilder<AppUpdateJob>(
                AppUpdatePolicy.CHECK_INTERVAL_HOURS,
                TimeUnit.HOURS,
                6,
                TimeUnit.HOURS,
            )
                .addTag(TAG)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        fun cancelTask(context: Context) {
            // cancel and remove job
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
            WorkManager.getInstance(context).pruneWork()
        }
    }
}
