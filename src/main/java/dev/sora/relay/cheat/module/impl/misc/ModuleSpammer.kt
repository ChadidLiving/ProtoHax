package dev.sora.relay.cheat.module.impl.misc

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.module.impl.combat.ModuleTargets
import dev.sora.relay.cheat.value.Choice
import dev.sora.relay.game.entity.EntityPlayer
import dev.sora.relay.game.event.EventTick
import dev.sora.relay.utils.getRandomString
import dev.sora.relay.utils.timing.MillisecondTimer
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.random.Random

class ModuleSpammer : CheatModule("刷屏", CheatCategory.MISC) {

	private var modeValue by choiceValue("模式", arrayOf(spam(), killsay()), "刷屏")
    private var messageValue by stringValue("内容", "Protohax_Metal")
	private var randomSuffix by boolValue("随机序列号", true)

	private fun sendMessage(placeholders: Map<String, String>) {
		session.sendPacket(TextPacket().apply {
			type = TextPacket.Type.CHAT
			xuid = session.player.xuid
			sourceName = session.player.username
			platformChatId = ""
			message = messageValue.let { if (randomSuffix) "$it >${getRandomString(10 + Random.nextInt(5))}<" else it }.let {
				var result = it
				placeholders.forEach { (k, v) -> result = result.replace(k, v) }
				result
			}
		})
	}

	private inner class Spam : Choice("直接发送") {

		private var delayValue by intValue("延迟", 5000, 500..10000)

		private val spamTimer = MillisecondTimer()

		private val handleTick = handle<EventTick> {
			if (spamTimer.hasTimePassed(delayValue)) {
				sendMessage(emptyMap())
				spamTimer.reset()
			}
		}
	}

	private inner class KillSay : Choice("击杀") {

		private val handleTargetKilled = handle<ModuleTargets.EventTargetKilled> {
			if (target is EntityPlayer) {
				sendMessage(mapOf("\$name" to target.username))
			}
		}
	}
}
