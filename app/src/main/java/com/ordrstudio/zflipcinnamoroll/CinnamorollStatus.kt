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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class StatusPercentage (
    val hunger: Float,
    val tired: Float,
    val lonely: Float,
    val bored: Float,
)

@Composable
fun CinnamorollStatus(state: CinnamorollState, modifier: Modifier = Modifier) {
//    var rerender by rememberSaveable { mutableStateOf (false) }
    var status by remember { mutableStateOf (state.statusAsPercentage()) }

    val doRerender: () -> Unit = {
//        rerender = !rerender
        status = state.statusAsPercentage()
    }

    LaunchedEffect(state) {
        while (true) {
            doRerender()
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
                    progress = { status.hunger },
                    color = Color.Red
                )
                Text("\uD83C\uDF4E", fontSize = 20.sp)
            }
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { status.tired },
                    color = Color.Blue
                )
                Text("\uD83D\uDCA4", fontSize = 20.sp)
            }
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { status.lonely },
                    color = Color.Green
                )
                Text("\uD83E\uDEC2", fontSize = 20.sp)
            }
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { status.bored },
                    color = Color.Gray
                )
                Text("\uD83E\uDD71", fontSize = 20.sp)
            }
        }
    }
}