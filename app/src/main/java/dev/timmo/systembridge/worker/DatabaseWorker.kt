package dev.timmo.systembridge.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.timmo.systembridge.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            if (inputData.keyValueMap["host"] !== null) {
                database.connectionDao().insert()
                Result.success()
            } else {
                Log.e(TAG, "Invalid input data")
                Result.failure()
            }

        } catch (ex: Exception) {
            Log.e(TAG, "Error", ex)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "DatabaseWorker"
    }
}