package com.ordrstudio.zflipcinnamoroll

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getDrawable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun CinnamorollSprite(
    state: CinnamorollState,
    setDrag: (Boolean) -> Unit,
    gameLogic: GameLogic,
    modifier: Modifier = Modifier
) {
    var rerender by rememberSaveable { mutableStateOf (false) }
    var actionState by rememberSaveable { mutableStateOf (state.actionState) }
    var isBeingPetted by rememberSaveable { mutableStateOf (state.isBeingPetted) }
    var isListeningToMusic by rememberSaveable { mutableStateOf (state.isListeningToMusic) }
    var isDepressed by rememberSaveable { mutableStateOf (state.isDepressed()) }
    val doRerender: () -> Unit = {
        rerender = !rerender
        actionState = state.actionState
        isBeingPetted = state.isBeingPetted
        isListeningToMusic = state.isListeningToMusic
        isDepressed = state.isDepressed()
    }
    LaunchedEffect(rerender) {
        while (true) {
            val shouldRerender = gameLogic.shouldRerenderSprite(state)
            if (shouldRerender) doRerender()
            delay(250)
        }
    }

    val resourceId = when (actionState) {
        ActionState.Idling ->
            if (isBeingPetted) R.drawable.cinnamorollcontent
            else if (isListeningToMusic ) R.drawable.cinnamorollmusic
        else if (isDepressed)
            R.drawable.cinnamorolldepressed
        else
            R.drawable.cinnamorollidle

        ActionState.Eating -> R.drawable.cinnamorolleat
        ActionState.Gaming -> R.drawable.cinnamorollthinking
        ActionState.Sleeping -> R.drawable.cinnamorollsleep
    }
    Image(
        painter = rememberDrawablePainter(
            drawable = getDrawable(
                LocalContext.current,
                resourceId
            )
        ),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .draggable(
                onDragStarted = { setDrag(true) },
                onDragStopped = { setDrag(false) },
                state = (rememberDraggableState {}),
                orientation = Orientation.Horizontal
            )
            .fillMaxWidth()
            .fillMaxHeight()
    )
}