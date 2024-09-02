package com.ordrstudio.zflipcinnamoroll

import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
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
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class WidgetDataStore(private val context: Context): DataStore<StatusPercentage> {
    suspend fun getStatus(): StatusPercentage {
        val db = Firebase.firestore
        val stateRef = db.collection(CINNAMOROLL_STATE_FIREBASE_STRING).document(TEMPORARY_ID)
        var status = StatusPercentage(0f, 0f, 0f, 0f)

        val document = stateRef.get().await()
        if (document != null && document.data != null) {
            val state = document.toObject<CinnamorollState>()!!
            status = state.statusAsPercentage()
        }
        return status
    }

    override val data: Flow<StatusPercentage>
        get() { return flow { emit(getStatus()) } }

    override suspend fun updateData(transform: suspend (t: StatusPercentage) -> StatusPercentage): StatusPercentage {
        throw NotImplementedError("Not implemented")
    }
}

class CinnamorollAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<StatusPercentage>
        get() = object: GlanceStateDefinition<StatusPercentage> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<StatusPercentage> {
                return WidgetDataStore(context)
            }

            override fun getLocation(context: Context, fileKey: String): File {
                throw NotImplementedError("Not implemented")
            }
        }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // create your AppWidget here
            GlanceTheme {
                CinnamorollWidget(currentState())
            }
        }
    }

    @Composable
    private fun CinnamorollWidget(status: StatusPercentage) {
        val date = Date(status.lastUpdated)
        val format = SimpleDateFormat("HH:mm")
        val time = format.format(date)

        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(GlanceTheme.colors.background)
                .clickable(actionStartActivity<MainActivity>()),
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
            Text("At $time")
        }
    }
}