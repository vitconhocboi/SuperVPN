//package com.core.config.data.helper
//
//import android.content.Context
//import androidx.hilt.work.HiltWorker
//import androidx.work.Constraints
//import androidx.work.ExistingWorkPolicy
//import androidx.work.NetworkType
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import com.google.android.gms.tasks.Tasks
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
//import com.google.firebase.remoteconfig.ktx.remoteConfig
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import com.core.config.AppPreferences
//import java.io.IOException
//import java.util.concurrent.ExecutionException
//import java.util.concurrent.TimeUnit
//import java.util.concurrent.TimeoutException
//
//@HiltWorker
//internal class RemoteConfigFetcherWorker @AssistedInject constructor(
//    private val appPreferences: AppPreferences,
//    @Assisted appContext: Context,
//    @Assisted workerParams: WorkerParameters,
//) : Worker(appContext, workerParams) {
//
//    companion object {
//
//        fun enqueue(context: Context) {
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//
//            val workRequestBuilder = OneTimeWorkRequestBuilder<RemoteConfigFetcherWorker>().setConstraints(constraints)
//
//            WorkManager.getInstance(context).enqueueUniqueWork(
//                RemoteConfigFetcherWorker::class.java.simpleName,
//                ExistingWorkPolicy.KEEP, workRequestBuilder.build()
//            )
//        }
//
//    }
//
//    override fun doWork(): Result {
//        try {
//            // Block on the task for a maximum of 60 seconds, otherwise time out.
//            val taskResult = Tasks.await(Firebase.remoteConfig.fetchAndActivate(), 60, TimeUnit.SECONDS)
//            appPreferences.isRemoteConfigStale = false
//            return Result.success()
//        } catch (e: ExecutionException) {
//            return if (e.cause is FirebaseRemoteConfigClientException && e.cause?.cause is IOException) {
//                Result.retry()
//            } else {
//                Result.failure()
//            }
//        } catch (e: InterruptedException) {
//            return Result.retry()
//        } catch (e: TimeoutException) {
//            return Result.retry()
//        } catch (e: Exception) {
//            return Result.failure()
//        }
//    }
//}