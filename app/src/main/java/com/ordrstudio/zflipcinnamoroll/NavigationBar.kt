package com.ordrstudio.zflipcinnamoroll

import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NavigationBar(
    state: CinnamorollState,
    toggleDisplayHUD: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rerender by rememberSaveable { mutableStateOf (false) }
    var canIdle by rememberSaveable { mutableStateOf (state.canIdle()) }
    var canEat by rememberSaveable { mutableStateOf (state.canEat()) }
    var canGame by rememberSaveable { mutableStateOf (state.canGame()) }
    var canSleep by rememberSaveable { mutableStateOf (state.canSleep()) }

    val doRerender: () -> Unit = {
        rerender = !rerender
        canIdle = state.canIdle()
        canEat = state.canEat()
        canGame = state.canGame()
        canSleep = state.canSleep()
    }
    LaunchedEffect(rerender) {
        while (true) {
            doRerender()
            delay(1000)
        }
    }

    BottomAppBar(
        modifier = Modifier
            .height(84.dp)
        ,
        containerColor = Color.LightGray,
        contentColor = MaterialTheme.colorScheme.primary,
        actions = {
            TextButton(
                onClick = { state.idle() },
                enabled = canIdle,
            ) {
                Text("\uD83D\uDE42", fontSize = 20.sp)
            }
            TextButton(
                onClick = { state.eat() },
                enabled = canEat,
            ) {
                Text("\uD83C\uDF7D\uFE0F", fontSize = 20.sp)
            }
            TextButton(
                onClick = { state.playGame() },
                enabled = canGame,
            ) {
                Text("\uD83C\uDFAE", fontSize = 20.sp)
            }
            TextButton(
                onClick = { state.sleep() },
                enabled = canSleep,
            ) {
                Text("\uD83D\uDCA4", fontSize = 20.sp)
            }
            TextButton(
                onClick = { toggleDisplayHUD() },
            ) {
                Text("\uD83D\uDCCA", fontSize = 20.sp)
            }
        }
    )
}