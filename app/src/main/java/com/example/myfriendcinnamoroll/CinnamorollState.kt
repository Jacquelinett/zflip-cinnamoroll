package com.example.myfriendcinnamoroll

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

const val TIME_TO_STARVE = 60000
const val TIME_TO_DEPRESSION = 20000
const val TIME_TO_LETHARGY = 30000
const val TIME_TO_BOREDOM = 60000
const val EAT_DURATION = 5000
const val HUNGER_RECOVERY_RATE = 15000
const val MUSIC_BOREDOM_RECOVERY_RATE = 500

enum class ActionState {
    Idling, Eating, Sleeping, Gaming, Listening
}

data class HealthStatus (
    val hungerProgress: Float,
    val sleepyProgress: Float,
    val lonelyProgress: Float,
    val boredProgress: Float,
)

@Parcelize
data class CinnamorollState (
    var actionState: ActionState,
    var lastStarved: Long = System.currentTimeMillis(),
    var lastDepression: Long = System.currentTimeMillis(),
    var lastLethargy: Long = System.currentTimeMillis(),
    var lastBoredom: Long = System.currentTimeMillis(),
    var startedEating: Long = System.currentTimeMillis(),
    var startedSleeping: Long = System.currentTimeMillis(),
    var startedListening: Long = System.currentTimeMillis(),
    var shouldRerenderOnNegativeStat: Boolean = true,
) : Parcelable {
    val canIdle: Boolean get() = (actionState != ActionState.Idling && actionState != ActionState.Eating)
    val isStarving: Boolean get() = (System.currentTimeMillis() - lastStarved) > TIME_TO_STARVE
    val isDepressed: Boolean get() = (System.currentTimeMillis() - lastDepression) > TIME_TO_DEPRESSION
    val isLethargic: Boolean get() = (System.currentTimeMillis() - lastLethargy) > TIME_TO_LETHARGY
    val isBoredom: Boolean get() = (System.currentTimeMillis() - lastBoredom) > TIME_TO_BOREDOM
}
