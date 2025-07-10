package dev.sora.relay.cheat.module.impl.movement

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.module.impl.combat.ModuleTargets
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.event.EventTick
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ModuleOpFightBot : CheatModule("环绕", CheatCategory.MOVEMENT) {

    private var modeValue by listValue("模式", Mode.values(), Mode.STRAFE)
    private var rangeValue by floatValue("距离", 1.5f, 1.5f..4f)
	private var passiveValue by boolValue("被动技能", false)
    private var horizontalSpeedValue by floatValue("水平速度", 5f, 1f..7f)
    private var verticalSpeedValue by floatValue("垂直速度", 4f, 1f..7f)
    private var strafeSpeedValue by intValue("环绕速度", 20, 10..90).visible { modeValue == Mode.STRAFE }

	private val handleTick = handle<EventTick> {
		val moduleTargets = moduleManager.getModule(ModuleTargets::class.java)
		val target = session.level.entityMap.values.filter { with(moduleTargets) { it.isTarget() } }
			.minByOrNull { it.distanceSq(session.player) } ?: return@handle
		if(target.distance(session.player) < 5) {
			val direction = Math.toRadians(when(modeValue) {
				Mode.RANDOM -> Math.random() * 360
				Mode.STRAFE -> ((session.player.tickExists * strafeSpeedValue) % 360).toDouble()
				Mode.BEHIND -> target.rotationYaw + 180.0
			}).toFloat()
			session.player.teleport(target.posX - sin(direction) * rangeValue, target.posY + 0.5f, target.posZ + cos(direction) * rangeValue)
		} else if (!passiveValue) {
			val direction = atan2(target.posZ - session.player.posZ, target.posX - session.player.posX) - Math.toRadians(90.0).toFloat()
			session.player.teleport(session.player.posX - sin(direction) * horizontalSpeedValue,
				target.posY.coerceIn(session.player.posY - verticalSpeedValue, session.player.posY + verticalSpeedValue),
				session.player.posZ + cos(direction) * horizontalSpeedValue)
		}
	}

	private enum class Mode(override val choiceName: String) : NamedChoice {
        RANDOM("随机方向"),
        STRAFE("环绕"),
        BEHIND("背后")
    }
}
