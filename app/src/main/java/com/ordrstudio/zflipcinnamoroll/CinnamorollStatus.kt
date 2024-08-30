package com.ordrstudio.zflipcinnamoroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class HealthStatus (
    val hungerProgress: Float,
    val sleepyProgress: Float,
    val lonelyProgress: Float,
    val boredProgress: Float,
)

fun getHealthStatus(state: CinnamorollState): HealthStatus {
    val currentTime = System.currentTimeMillis()
    var hungerProgress: Double = 0.0
    var sleepyProgress: Double = 0.0
    if (state.actionState == ActionState.Eating) {
        hungerProgress = (TIME_TO_STARVE - (state.startedEating - state.lastStarved)).toDouble() / TIME_TO_STARVE
    } else {
        hungerProgress = (TIME_TO_STARVE - (currentTime - state.lastStarved)).toDouble() / TIME_TO_STARVE
    }

    var sleeplessInterval = TIME_TO_LETHARGY - (currentTime - state.lastLethargy)
    if (sleeplessInterval < 0) sleeplessInterval = 0
    if (state.actionState == ActionState.Sleeping) {
        val sleepingInterval = (currentTime - state.startedSleeping) * 2
        sleepyProgress = (sleeplessInterval + sleepingInterval).toDouble() / TIME_TO_LETHARGY
    } else {
        sleepyProgress = (sleeplessInterval).toDouble() / TIME_TO_LETHARGY
    }

    val lonelyProgress = (TIME_TO_DEPRESSION - (currentTime - state.lastDepression)).toDouble() / TIME_TO_DEPRESSION

    var boredProgress: Double = 0.0
    var noMusicInterval = TIME_TO_BOREDOM - (currentTime - state.lastBoredom)
    if (noMusicInterval < 0) noMusicInterval = 0
    if (state.actionState == ActionState.Listening) {
        val musicInterval = (currentTime - state.startedListening) * 2
        boredProgress = (noMusicInterval + musicInterval).toDouble() / TIME_TO_BOREDOM
    } else {
        boredProgress = (noMusicInterval).toDouble() / TIME_TO_BOREDOM
    }

    return HealthStatus(hungerProgress.toFloat(), sleepyProgress.toFloat(), lonelyProgress.toFloat(), boredProgress.toFloat())
}

@Composable
fun CinnamorollStatus(state: CinnamorollState, modifier: Modifier = Modifier) {
    var healthStatus by remember { mutableStateOf(getHealthStatus(state)) }

    LaunchedEffect(state) {
        while (true) {
            healthStatus = getHealthStatus(state)
            delay(250)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .height(intrinsicSize = IntrinsicSize.Max)
                .padding(vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { healthStatus.hungerProgress },
                    color = Color.Red
                )
                Text("\uD83C\uDF4E", fontSize = 20.sp)
            }
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { healthStatus.sleepyProgress },
                    color = Color.Blue
                )
                Text("\uD83D\uDCA4", fontSize = 20.sp)
            }
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { healthStatus.lonelyProgress },
                    color = Color.Green
                )
                Text("\uD83E\uDEC2", fontSize = 20.sp)
            }
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { healthStatus.boredProgress },
                    color = Color.Gray
                )
                Text("\uD83E\uDD71", fontSize = 20.sp)
            }
        }
    }
}