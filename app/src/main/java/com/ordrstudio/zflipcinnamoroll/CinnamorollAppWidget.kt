package com.ordrstudio.zflipcinnamoroll

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class CinnamorollAppWidget : GlanceAppWidget() {
    private var state = CinnamorollState()

    private val db = Firebase.firestore
    private val stateRef = db.collection(CINNAMOROLL_STATE_FIREBASE_STRING).document(TEMPORARY_ID)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        println("We rerendering")
        var isError = false;
        var isLoading = true;

        val document = stateRef.get().await()
        if (document != null && document.data != null) {
            state = document.toObject<CinnamorollState>()!!
            isError = false
            isLoading = false
        }

        provideContent {
            // create your AppWidget here
            GlanceTheme {
                if (isError) {
                    Text(text = "Errored")
//                    ErrorView()
                } else if (isLoading) {
                    Text(text = "Loading...")
                } else {
                    CinnamorollWidget(state)
                }
            }
        }
    }

    @Composable
    private fun CinnamorollWidget(state: CinnamorollState) {
        val status = state.statusAsPercentage()
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(GlanceTheme.colors.background)
                .clickable {
                    actionStartActivity<MainActivity>()
                },
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text("\uD83C\uDF4E")
                LinearProgressIndicator(
                    progress = status.hunger,
                    color = ColorProvider(Color.Red)
                )
            }
            Row {
                Text("\uD83D\uDCA4")
                LinearProgressIndicator(
                    progress = status.tired,
                    color = ColorProvider(Color.Blue)
                )

            }
            Row {
                Text("\uD83E\uDEC2")
                LinearProgressIndicator(
                    progress = status.lonely,
                    color = ColorProvider(Color.Green)
                )
            }
            Row {
                Text("\uD83E\uDD71")
                LinearProgressIndicator(
                    progress = status.bored,
                    color = ColorProvider(Color.Gray)
                )
            }
        }
    }
}