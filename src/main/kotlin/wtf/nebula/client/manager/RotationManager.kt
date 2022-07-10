package wtf.nebula.client.manager

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.motion.update.PreMotionUpdate
import wtf.nebula.util.Globals
import wtf.nebula.util.rotation.InvalidRotation
import wtf.nebula.util.rotation.Rotation

class RotationManager : Globals {
    var rotation: Rotation = InvalidRotation()
        set(value) {
            field = value
            rotationTicks = 0
        }

    private var rotationTicks = 0
        set(value) {
            field = value
            if (value >= 10) {
                rotation = InvalidRotation()
            }
        }

    @EventListener
    private val preMotionUpdateListener = listener<PreMotionUpdate> {
        ++rotationTicks
        if (rotation.valid) {
            it.yaw = rotation.yaw
            it.pitch = rotation.pitch

            it.cancel()
        }
    }
}