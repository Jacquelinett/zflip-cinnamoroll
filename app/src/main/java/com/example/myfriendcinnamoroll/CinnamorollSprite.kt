package com.example.myfriendcinnamoroll

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getDrawable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlin.math.abs

@Composable
fun CinnamorollSprite(state: CinnamorollState, onDrag: (Long) -> Unit, modifier: Modifier = Modifier) {
    var petting by remember { mutableStateOf<Boolean>(false) }
    var resourceId: Int = R.drawable.cinnamorollidle
    resourceId = when (state.actionState) {
        ActionState.Idling -> if (petting)
            R.drawable.cinnamorollcontent
        else if (state.isDepressed)
            R.drawable.cinnamorolldepressed
        else
            R.drawable.cinnamorollidle

        ActionState.Eating -> R.drawable.cinnamorolleat
        ActionState.Gaming -> R.drawable.cinnamorollthinking
        ActionState.Sleeping -> R.drawable.cinnamorollsleep
        ActionState.Listening -> R.drawable.cinnamorollmusic
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
                onDragStarted = { petting = true },
                onDragStopped = { petting = false },
                state = (rememberDraggableState { delta -> onDrag( abs(delta).toLong() ) }),
                orientation = Orientation.Horizontal
            )
            .fillMaxWidth()
            .fillMaxHeight()
    )
}