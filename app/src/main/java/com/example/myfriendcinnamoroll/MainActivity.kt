package com.example.myfriendcinnamoroll

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getDrawable
import com.example.myfriendcinnamoroll.ui.theme.MyFriendCinnamorollTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import kotlin.math.abs

const val FLIP_SCREEN_WIDTH = 720 / 2
const val FLIP_SCREEN_HEIGHT = 748 / 2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setContent {
            MyFriendCinnamoroll(audioManager = audioManager)
        }
    }

    @Preview(
        showBackground = true,
        widthDp = FLIP_SCREEN_WIDTH,
        heightDp = FLIP_SCREEN_HEIGHT,
    )
    @Composable
    fun MyFriendCinnamorollPreview() {
        MyFriendCinnamoroll()
    }

    @Composable
    fun MyFriendCinnamoroll(audioManager: AudioManager? = null) {
        MyFriendCinnamorollTheme {
            var state by rememberSaveable { mutableStateOf(CinnamorollState(ActionState.Idling)) }

            val updateLastBoredom: (Long) -> Unit = { increaseBy: Long ->
                run {
                    val currentTime = System.currentTimeMillis()
                    var newLastBoredom = state.lastBoredom
                    if (newLastBoredom < currentTime - TIME_TO_BOREDOM) newLastBoredom =
                        currentTime - TIME_TO_BOREDOM
                    newLastBoredom += increaseBy
                    if (newLastBoredom > currentTime) newLastBoredom = currentTime
                    state = state.copy(lastBoredom = newLastBoredom)
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
                        shouldRerenderOnNegativeStat = true
                    )
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
                }
            }

            var shouldDisplayHUD by remember { mutableStateOf(true) }
            val toggleeDisplayHUD: () -> Unit = {
                shouldDisplayHUD = !shouldDisplayHUD
            }

            LaunchedEffect(state) {
                while (true) {
                    val currentTime = System.currentTimeMillis()

                    if (state.shouldRerenderOnNegativeStat) {
                        if (state.isDepressed) state =
                            state.copy(shouldRerenderOnNegativeStat = false)
                    }

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