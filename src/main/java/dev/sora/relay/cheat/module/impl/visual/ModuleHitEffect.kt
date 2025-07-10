package dev.sora.relay.cheat.module.impl.visual

import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.module.impl.combat.ModuleTargets
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.GameSession
import dev.sora.relay.game.entity.Entity
import dev.sora.relay.game.entity.EntityPlayer
import dev.sora.relay.game.event.EventPacketOutbound
import dev.sora.relay.game.registry.BlockDefinition
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.bedrock.data.LevelEvent
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket

class ModuleHitEffect : CheatModule("打击粒子", CheatCategory.VISUAL) {

	private var effectValue by listValue("效果", Effect.values(), Effect.CRITICAL)

	private val onPacketOutbound = handle<EventPacketOutbound> {
		if (packet is InventoryTransactionPacket && packet.transactionType == InventoryTransactionType.ITEM_USE_ON_ENTITY && packet.actionType == 1) {
			val target = session.level.entityMap[packet.runtimeEntityId] ?: return@handle
			if (with(moduleManager.getModule(ModuleTargets::class.java)) { target.isTarget() }) {
				effectValue.deployEffect(session, target)
			}
		}
	}

	private enum class Effect(override val choiceName: String) : NamedChoice {
		CRITICAL("暴击粒子") {
			override fun deployEffect(session: GameSession, target: Entity) {
				session.netSession.inboundPacket(AnimatePacket().apply {
					runtimeEntityId = target.runtimeEntityId
					action = AnimatePacket.Action.CRITICAL_HIT
				})
			}
		},
		BLOOD("血液") {

			private val definition by lazy { BlockDefinition(0, "minecraft:redstone_block", NbtMap.EMPTY) }

			override fun deployEffect(session: GameSession, target: Entity) {
				session.netSession.inboundPacket(LevelEventPacket().apply {
					type = LevelEvent.PARTICLE_DESTROY_BLOCK
					position = if (target is EntityPlayer) target.vec3PositionFeet.add(0f, 1f, 0f) else target.vec3Position
					data = session.blockMapping.getRuntimeByDefinition(definition)
				})
			}
		},
		LAVA("岩浆") {

			private val definition by lazy { BlockDefinition(0, "minecraft:lava", NbtMap.builder()
				.putInt("liquid_depth", 15).build()) }

			override fun deployEffect(session: GameSession, target: Entity) {
				session.netSession.inboundPacket(LevelEventPacket().apply {
					type = LevelEvent.PARTICLE_DESTROY_BLOCK
					position = if (target is EntityPlayer) target.vec3PositionFeet.add(0f, 1f, 0f) else target.vec3Position
					data = session.blockMapping.getRuntimeByDefinition(definition)
				})
			}
		};

		abstract fun deployEffect(session: GameSession, target: Entity)
	}
}
