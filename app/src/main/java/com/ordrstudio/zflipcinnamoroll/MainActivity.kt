package com.ordrstudio.zflipcinnamoroll

import android.content.ContentValues.TAG
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import com.ordrstudio.zflipcinnamoroll.ui.theme.MyFriendCinnamorollTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.delay
import java.time.LocalDateTime

const val FLIP_SCREEN_WIDTH = 720 / 2
const val FLIP_SCREEN_HEIGHT = 748 / 2

const val CINNAMOROLL_STATE_FIREBASE_STRING = "cinnamorolls"

// NOTES: Get ID from authentication or something down the road
const val TEMPORARY_ID = "jacqueline"

class MainActivity : ComponentActivity() {
    val db = Firebase.firestore
    val stateRef = db.collection(CINNAMOROLL_STATE_FIREBASE_STRING).document(TEMPORARY_ID)
    var state = CinnamorollState()
    var stateCache = CinnamorollState()
    val setIsPetting: (Boolean) -> Unit = {
        petting: Boolean -> state.isBeingPetted = petting
    }
    val gameLogic = GameLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        stateRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    state = document.toObject<CinnamorollState>()!!
                    stateCache = state
                    Log.d(TAG, "state data: ${state}")
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

                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                setContent {
                    LaunchedEffect(state) {
                        while (true) {
                            val currentTime = System.currentTimeMillis()
                            state.calculateChange(currentTime, audioManager.isMusicActive() ?: false)
                            delay(250)
                        }
                    }
                    ZFlipCinnamoroll()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        setContent {
            Text("Loading...")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stateRef.set(stateCache)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    @Preview(
        showBackground = true,
        widthDp = FLIP_SCREEN_WIDTH,
        heightDp = FLIP_SCREEN_HEIGHT,
    )
    @Composable
    fun ZFlipCinnamorollPreview() {
        ZFlipCinnamoroll()
    }

    @Composable
    fun ZFlipCinnamoroll() {
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