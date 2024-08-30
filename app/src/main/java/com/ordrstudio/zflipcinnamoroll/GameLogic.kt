package com.ordrstudio.zflipcinnamoroll

class GameLogic(
    var previousActionState: ActionState = ActionState.Idling,
    var wasListeningToMusic: Boolean = false,
    var wasBeingPetted: Boolean = false,
    var wasDepressed: Boolean = false
) {
    fun shouldRerenderSprite(
        state: CinnamorollState,
    ) : Boolean {
        var shouldRerender = false

        if (wasBeingPetted != state.isBeingPetted) {
            wasBeingPetted = state.isBeingPetted
            shouldRerender = true
        }

        if (wasListeningToMusic != state.isListeningToMusic) {
            wasListeningToMusic = state.isListeningToMusic
            shouldRerender = true
        }

        if (wasDepressed != state.isDepressed()) {
            wasDepressed = state.isDepressed()
            shouldRerender = true
        }

        if (previousActionState != state.actionState) {
            shouldRerender = true
        }

        if (shouldRerender) {
            println("" + previousActionState + " " + state.actionState)
        }

        previousActionState = state.actionState

        return shouldRerender
    }
}