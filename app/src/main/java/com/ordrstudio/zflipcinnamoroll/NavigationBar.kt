package com.ordrstudio.zflipcinnamoroll

import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationBar(
    state: CinnamorollState,
    toggleDisplayHUD: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by rememberSaveable { mutableIntStateOf(1 ) }
    val setSelected: (Int) -> Unit = { value: Int ->
        run {
            selected = value
            when (value) {
                1 -> state.idle()
                2 -> state.eat()
                3 -> state.playGame()
                4 -> state.sleep()
            }

        }
    }

    fun shouldEnable(num: Int) : Boolean {
        return !state.isBusy() && num != selected
    }

    BottomAppBar(
        modifier = Modifier
            .height(84.dp)
        ,
        containerColor = Color.LightGray,
        contentColor = MaterialTheme.colorScheme.primary,
        actions = {
            TextButton(
                onClick = { setSelected(1) },
                enabled = !state.canIdle(),
            ) {
                Text("\uD83D\uDE42", fontSize = 20.sp)
            }
            TextButton(
                onClick = { setSelected(2) },
                enabled = !state.canEat(),
            ) {
                Text("\uD83C\uDF7D\uFE0F", fontSize = 20.sp)
            }
            TextButton(
                onClick = { setSelected(3)},
                enabled = !state.canGame(),
            ) {
                Text("\uD83C\uDFAE", fontSize = 20.sp)
            }
            TextButton(
                onClick = {  setSelected(4) },
                enabled = !state.canSleep(),
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