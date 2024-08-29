package com.example.myfriendcinnamoroll

import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationBar(
    state: CinnamorollState,
    updateActionState: (actionState: ActionState) -> Unit,
    toggleeDisplayHUD: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = Modifier
            .height(84.dp)
        ,
        containerColor = Color.LightGray,
        contentColor = MaterialTheme.colorScheme.primary,
        actions = {
            TextButton(
                onClick = { updateActionState(ActionState.Idling) },
                enabled = state.canIdle,
            ) {
                Text("\uD83D\uDE42", fontSize = 20.sp)
            }
            TextButton(
                onClick = { updateActionState(ActionState.Eating) },
                enabled = state.actionState == ActionState.Idling,
            ) {
                Text("\uD83C\uDF7D\uFE0F", fontSize = 20.sp)
            }
            TextButton(
                onClick = { updateActionState(ActionState.Gaming) },
                enabled = state.actionState == ActionState.Idling,
            ) {
                Text("\uD83C\uDFAE", fontSize = 20.sp)
            }
            TextButton(
                onClick = { updateActionState(ActionState.Sleeping) },
                enabled = state.actionState == ActionState.Idling,
            ) {
                Text("\uD83D\uDCA4", fontSize = 20.sp)
            }
            TextButton(
                onClick = { toggleeDisplayHUD() },
            ) {
                Text("\uD83D\uDCCA", fontSize = 20.sp)
            }
        }
    )
}