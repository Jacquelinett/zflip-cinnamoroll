package com.ordrstudio.zflipcinnamoroll

import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class BackgroundUpdateWorker(context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {
    private var state = CinnamorollState()

    private val db = Firebase.firestore
    private val stateRef = db.collection(CINNAMOROLL_STATE_FIREBASE_STRING).document(TEMPORARY_ID)

    override suspend fun doWork(): Result {
        var isError = true;
        val document = stateRef.get().await()
        if (document != null && document.data != null) {
            state = document.toObject<CinnamorollState>()!!
            state.calculateChange(System.currentTimeMillis())
            stateRef.set(state).await()
            CinnamorollAppWidget().updateAll(applicationContext)
            isError = false
        }

        // Indicate whether the work finished successfully with the Result
        return if (isError) Result.failure() else Result.success()
    }
}
