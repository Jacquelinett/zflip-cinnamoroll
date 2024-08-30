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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        println("onCreate run")
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
                    ZFlipCinnamoroll(audioManager = audioManager)
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
        println(stateCache)
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
    fun ZFlipCinnamoroll(audioManager: AudioManager? = null) {
        MyFriendCinnamorollTheme {
            var state by rememberSaveable { mutableStateOf(state) }

            val saveState: () -> Unit = {
                state = state.copy(
                    lastUpdated =  LocalDateTime.now().toString()
                )
                stateCache = state
            }

            val updateLastBoredom: (Long) -> Unit = { increaseBy: Long ->
                run {
                    val currentTime = System.currentTimeMillis()
                    var newLastBoredom = state.lastBoredom
                    if (newLastBoredom < currentTime - TIME_TO_BOREDOM) newLastBoredom =
                        currentTime - TIME_TO_BOREDOM
                    newLastBoredom += increaseBy
                    if (newLastBoredom > currentTime) newLastBoredom = currentTime
                    state = state.copy(lastBoredom = newLastBoredom)
                    saveState()
                }
            }

            val updateLastLethargy: (Long) -> Unit = { increaseBy: Long ->
                run {
                    val currentTime = System.currentTimeMillis()
                    var newLastLethargy = state.lastDepression
                    if (newLastLethargy < currentTime - TIME_TO_LETHARGY) newLastLethargy =
                        currentTime - TIME_TO_LETHARGY
                    newLastLethargy += increaseBy
                    if (newLastLethargy > currentTime) newLastLethargy = currentTime
                    state = state.copy(lastLethargy = newLastLethargy)
                    saveState()
                }
            }

            val updateLastDepression: (Long) -> Unit = { increaseBy: Long ->
                run {
                    val currentTime = System.currentTimeMillis()
                    var newLastDepression = state.lastDepression
                    if (newLastDepression < currentTime - TIME_TO_DEPRESSION) newLastDepression =
                        currentTime - TIME_TO_DEPRESSION
                    newLastDepression += increaseBy
                    if (newLastDepression > currentTime) newLastDepression = currentTime
                    state = state.copy(
                        lastDepression = newLastDepression,
                    )
                    saveState()
                }
            }
            val updateLastStarved: () -> Unit = {
                run {
                    val currentTime = System.currentTimeMillis()
                    var newLastStarved = state.lastStarved
                    if (newLastStarved < currentTime - TIME_TO_STARVE) newLastStarved =
                        currentTime - TIME_TO_STARVE
                    newLastStarved += HUNGER_RECOVERY_RATE
                    if (newLastStarved > currentTime) newLastStarved = currentTime
                    state =
                        state.copy(actionState = ActionState.Idling, lastStarved = newLastStarved)
                    saveState()
                }
            }

            val updateActionState: (ActionState) -> Unit = { actionState: ActionState ->
                run {
                    val currentTime = System.currentTimeMillis()
                    if (actionState == ActionState.Eating) {
                        state = state.copy(startedEating = currentTime)
                    } else if (actionState == ActionState.Sleeping) {
                        state = state.copy(startedSleeping = currentTime)
                    } else if (actionState == ActionState.Listening) {
                        state = state.copy(startedListening = currentTime)
                    } else if (actionState == ActionState.Idling) {
                        if (state.actionState == ActionState.Sleeping) {
                            updateLastLethargy((currentTime - state.startedSleeping) * 2)
                        } else if (state.actionState == ActionState.Listening) {
                            updateLastBoredom((currentTime - state.startedListening) * 2)
                        }
                    }
                    state = state.copy(actionState = actionState)
                    saveState()
                }
            }

            var shouldDisplayHUD by remember { mutableStateOf(true) }
            val toggleeDisplayHUD: () -> Unit = {
                shouldDisplayHUD = !shouldDisplayHUD
            }

            LaunchedEffect(state) {
                while (true) {
                    val currentTime = System.currentTimeMillis()

                    if (state.actionState == ActionState.Eating) {
                        if (currentTime - state.startedEating > EAT_DURATION) {
                            updateLastStarved()
                        }
                    } else if (state.actionState == ActionState.Idling) {
                        if (audioManager?.isMusicActive() == true) {
                            updateActionState(ActionState.Listening)
                        }
                    } else if (state.actionState == ActionState.Listening) {
                        if (audioManager?.isMusicActive() == false) {
                            updateActionState(ActionState.Idling)
                        }
                    }
//
                    delay(250)
                }
            }

            Scaffold(
                topBar = {
                    Row() {
                        if (shouldDisplayHUD) {
                            CinnamorollStatus(state = state)
                        }
                    }
                },
                bottomBar = { NavigationBar(state, updateActionState, toggleeDisplayHUD) },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .background(color = Color(202, 240, 255))
                    ) {
                        CinnamorollSprite(
                            state,
                            updateLastDepression
                        )
                    }
                },
            )
        }
    }
}