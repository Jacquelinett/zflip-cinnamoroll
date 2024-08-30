package com.ordrstudio.zflipcinnamoroll

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

const val TIME_TO_STARVE = 60000
const val TIME_TO_DEPRESSION = 30000
const val TIME_TO_LETHARGY = 30000
const val TIME_TO_BOREDOM = 60000
const val EAT_DURATION = 5000
const val HUNGER_RECOVERY_RATE = 15000
const val MUSIC_BOREDOM_RECOVERY_RATE = 500

enum class ActionState {
    Idling, Eating, Sleeping, Gaming, Listening
}

@Parcelize
data class CinnamorollState (
    var actionState: ActionState = ActionState.Idling,
    var lastStarved: Long = System.currentTimeMillis(),
    var lastDepression: Long = System.currentTimeMillis(),
    var lastLethargy: Long = System.currentTimeMillis(),
    var lastBoredom: Long = System.currentTimeMillis(),
    var startedEating: Long = System.currentTimeMillis(),
    var startedSleeping: Long = System.currentTimeMillis(),
    var startedListening: Long = System.currentTimeMillis(),
    var lastUpdated: String = LocalDateTime.now().toString()
) : Parcelable

fun CinnamorollState.canIdle(): Boolean {
    return actionState != ActionState.Idling && actionState != ActionState.Eating
}

fun CinnamorollState.isStarving(): Boolean {
    return (System.currentTimeMillis() - lastStarved) > TIME_TO_STARVE
}

fun CinnamorollState.isDepressed(): Boolean {
    return (System.currentTimeMillis() - lastDepression) > TIME_TO_DEPRESSION
}

fun CinnamorollState.isLethargic(): Boolean {
    return (System.currentTimeMillis() - lastLethargy) > TIME_TO_LETHARGY
}

fun CinnamorollState.isBoredom(): Boolean {
    return (System.currentTimeMillis() - lastBoredom) > TIME_TO_BOREDOM
}