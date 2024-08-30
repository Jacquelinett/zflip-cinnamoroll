package com.ordrstudio.zflipcinnamoroll

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Date
import kotlin.math.max
import kotlin.math.min

const val HUNGER_RATE = 0.5f
const val LONELY_RATE = 1f
const val BORED_RATE = 0.75f
const val TIRED_RATE = 0.25f

const val EAT_DURATION = 5000
const val HUNGER_RECOVERY_RATE = 15f
const val MUSIC_RECOVERY_RATE = 0.5f
const val PETTING_RECOVERY_RATE = 10f
const val SLEEP_RECOVERY_RATE = 5f

enum class ActionState {
    Idling, Eating, Sleeping, Gaming
}

@Parcelize
class CinnamorollState (
    var actionState: ActionState = ActionState.Idling,
    var hunger: Float = 100f,
    var tired: Float = 100f,
    var bored: Float = 100f,
    var lonely: Float = 100f,
    var lastUpdated: Long = System.currentTimeMillis(),
    var busySince: Long = System.currentTimeMillis(),

    @Exclude
    var isBeingPetted: Boolean = false,
    @Exclude
    var isListeningToMusic: Boolean = false,
) : Parcelable {
    fun canIdle(): Boolean {
        return actionState != ActionState.Idling && !isBusy()
    }

    fun canEat(): Boolean {
        return actionState != ActionState.Eating && !isBusy()
    }

    fun canSleep(): Boolean {
        return actionState != ActionState.Sleeping && !isBusy()
    }

    fun canGame(): Boolean {
        return actionState != ActionState.Gaming && !isBusy()
    }

    fun isBusy(): Boolean {
        return actionState == ActionState.Eating
    }

    fun isIdle(): Boolean {
        return actionState == ActionState.Idling
    }

    fun isEntertained(): Boolean {
        return actionState == ActionState.Gaming || isListeningToMusic
    }

    fun eat(): Unit {
        if (!isBusy()) {
            actionState = ActionState.Eating
            busySince = System.currentTimeMillis()
        }
    }

    fun playGame(): Unit {
        if (!isBusy()) actionState = ActionState.Gaming
    }

    fun sleep(): Unit {
        if (!isBusy()) actionState = ActionState.Sleeping
    }

    fun idle(): Unit {
        if (!isBusy()) actionState = ActionState.Idling
    }

    fun calculateChange(
        currentTime: Long,
        isMusicPlaying: Boolean = false,
    ): Unit {
        val delta = (currentTime - lastUpdated) / 1000f
        isListeningToMusic = isMusicPlaying

        // Calculate action state change
        when (actionState) {
            ActionState.Idling -> {

            }

            ActionState.Eating -> {
                if (currentTime - busySince > EAT_DURATION) {
                    actionState = ActionState.Idling
                    hunger += HUNGER_RECOVERY_RATE
                }
            }
            ActionState.Sleeping -> {
                tired = min(tired + SLEEP_RECOVERY_RATE * delta, 100f)
            }
            ActionState.Gaming -> {

            }
        }

        // Calculate general state changes
        if (actionState != ActionState.Eating) hunger = max(hunger - HUNGER_RATE * delta, 0f)

        if (actionState != ActionState.Sleeping) tired = max(tired - TIRED_RATE * delta, 0f)

        if (!isEntertained()) bored = max(bored - BORED_RATE * delta, 0f)

        if (isListeningToMusic) bored = min(bored + MUSIC_RECOVERY_RATE * delta, 100f)

        if (isBeingPetted) lonely = min(lonely + PETTING_RECOVERY_RATE * delta, 100f)
        else lonely = max(lonely - LONELY_RATE * delta, 0f)

        lastUpdated = currentTime
    }

    fun statusAsPercentage() : StatusPercentage {
        return StatusPercentage(
            hunger / 100,
            tired / 100,
            lonely / 100,
            bored / 100,
        )
    }

    fun isDepressed() : Boolean {
        return lonely <= 0
    }

    fun readableLastUpdated() : String {
        return Date(lastUpdated).toString()
    }
}