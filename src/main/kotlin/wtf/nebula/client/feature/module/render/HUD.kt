package wtf.nebula.client.feature.module.render

import me.bush.eventbuskotlin.EventListener
import me.bush.eventbuskotlin.listener
import wtf.nebula.client.Nebula
import wtf.nebula.client.event.render.RenderHUDEvent
import wtf.nebula.client.feature.module.Module
import wtf.nebula.client.feature.module.ModuleCategory
import wtf.nebula.client.registry.impl.ModuleRegistry

class HUD : Module(ModuleCategory.RENDER, "Renders an overlay of the vanilla HUD") {
    val watermark by bool("Watermark", true)
    val arraylist by bool("Arraylist", true)

    @EventListener
    private val renderHUDEvent = listener<RenderHUDEvent> {
        if (watermark) {
            mc.fontRenderer.drawStringWithShadow(WATERMARK, 2.0f, 2.0f, Colors.color())
        }

        if (arraylist) {
            val modules = ModuleRegistry.INSTANCE!!.registers
                .filter { mod -> mod.isActive() }
                .sortedBy { mod ->
                    val width = mc.fontRenderer.getStringWidth(mod.name)
                    mod.animation.max = width.toFloat()
                    return@sortedBy -width
                }

            var y = 2.0
            for (module in modules) {
                module.animation.update()

                val x = (it.res.scaledWidth - module.animation.value - 4.0)
                val height = mc.fontRenderer.FONT_HEIGHT + 2.0

                mc.fontRenderer.drawStringWithShadow(
                    module.name,
                    (x + 2.3).toFloat(),
                    (y + (height / 2.0) - (mc.fontRenderer.FONT_HEIGHT / 2.0)).toFloat(),
                    Colors.color())

                y += height
            }
        }
    }

    companion object {
        var WATERMARK = Nebula.FULL
    }
}