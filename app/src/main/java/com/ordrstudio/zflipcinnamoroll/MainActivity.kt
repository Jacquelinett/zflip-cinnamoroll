package com.ordrstudio.zflipcinnamoroll

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.media.AudioManager
import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ordrstudio.zflipcinnamoroll.ui.theme.MyFriendCinnamorollTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

const val FLIP_SCREEN_WIDTH = 720 / 2
const val FLIP_SCREEN_HEIGHT = 748 / 2

const val CINNAMOROLL_STATE_FIREBASE_STRING = "cinnamorolls"

// NOTES: Get ID from authentication or something down the road
const val TEMPORARY_ID = "jacqueline"

const val BACKGROUND_WORK_UNIQUE_NAME = "CINNAMOROLL_WORKER"


class MainActivity : ComponentActivity() {
    private var state = CinnamorollState()

    private val db = Firebase.firestore
    private val stateRef = db.collection(CINNAMOROLL_STATE_FIREBASE_STRING).document(TEMPORARY_ID)
    private var dataLoaded = false
    private lateinit var notificationHandler: NotificationHandler
    private val setIsPetting: (Boolean) -> Unit = {
        petting: Boolean -> state.isBeingPetted = petting
    }
    private val gameLogic = GameLogic()
    private lateinit var widgetManager: GlanceAppWidgetManager
    var widgetExist = false

    private val backgroundUpdateWorkRequest: PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<BackgroundUpdateWorker>(1, TimeUnit.HOURS)
            .build()

    suspend fun updateWidgets() {
        CinnamorollAppWidget().updateAll(this@MainActivity)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationHandler = NotificationHandler(this@MainActivity)
        widgetManager = GlanceAppWidgetManager(this)

        stateRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.data != null) {
                    state = document.toObject<CinnamorollState>()!!
                } else {
                    Log.d(TAG, "No such document")
                    stateRef.set(state)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot written")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                }

                dataLoaded = true

                runBlocking {
                    launch {
                        val glanceIds = widgetManager.getGlanceIds(CinnamorollAppWidget().javaClass)
                        if (glanceIds.isNotEmpty()) {
                            widgetExist = true
                            updateWidgets()
                        }
                    }
                }

                val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

                setContent {
                    ZFlipCinnamoroll(state)
                    LaunchedEffect(state) {
                        while (true) {
                            val currentTime = System.currentTimeMillis()
                            state.calculateChange(currentTime, audioManager.isMusicActive() ?: false)
                            delay(250)
                        }
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        setContent {
            Text("Loading...")
        }
    }

    override fun onResume() {
        super.onResume()
        notificationHandler.clearAlarm()
        WorkManager.getInstance(this@MainActivity).cancelUniqueWork(BACKGROUND_WORK_UNIQUE_NAME)
    }

    override fun onPause() {
        super.onPause()
        if (dataLoaded) {
            stateRef.set(state)
                .addOnSuccessListener { documentReference ->
                    notificationHandler.scheduleNotification(state)
                    if (widgetExist) {
                        WorkManager.getInstance(this@MainActivity)
                            .enqueueUniquePeriodicWork(BACKGROUND_WORK_UNIQUE_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, backgroundUpdateWorkRequest)
                        runBlocking {
                            launch {
                                updateWidgets()
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

//    @Preview(
//        showBackground = true,
//        widthDp = FLIP_SCREEN_WIDTH,
//        heightDp = FLIP_SCREEN_HEIGHT,
//    )
//    @Composable
//    fun ZFlipCinnamorollPreview() {
//        ZFlipCinnamoroll()
//    }

    @Composable
    fun ZFlipCinnamoroll(state: CinnamorollState = CinnamorollState()) {
        MyFriendCinnamorollTheme {
            var shouldDisplayHUD by remember { mutableStateOf(true) }
            val toggleDisplayHUD: () -> Unit = {
                shouldDisplayHUD = !shouldDisplayHUD
            }

            Scaffold(
                topBar = {
                    Row() {
                        if (shouldDisplayHUD) {
                            CinnamorollStatus(state = state)
                        }
                    }
                },
                bottomBar = { NavigationBar(state, toggleDisplayHUD) },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .background(color = Color(202, 240, 255))
                    ) {
                        CinnamorollSprite(
                            state,
                            setIsPetting,
                            gameLogic
                        )
                    }
                },
            )
        }
    }
}