package dev.grit.mechanic;

import dev.grit.Grit;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Страх темноты. Долгое пребывание в кромешной тьме под крышей/под землёй давит на психику:
 *  1 — шёпоты пещер звучат прямо рядом;
 *  2 — слышно собственное сердцебиение;
 *  3 — зрение пульсирует (ванильная «Тьма»), слабость;
 *  4 — паника: ещё и утомление.
 * Любой свет (факел, фонарь, светокамень) быстро снимает напряжение. Ночное зрение — тоже.
 */
public final class DreadHandler {
	private static final int CHECK_INTERVAL = 10;
	private static final int MAX_STAGE = 4;

	private DreadHandler() {}

	public static void tick(ServerPlayerEntity player, PlayerState state, int tick) {
		if ((tick + player.getId()) % CHECK_INTERVAL != 0) return;

		ServerWorld world = (ServerWorld) player.getWorld();
		boolean dark = isInDarkness(player, world);
		if (dark) {
			state.darkTicks += CHECK_INTERVAL;
		} else {
			state.darkTicks = Math.max(0, state.darkTicks - CHECK_INTERVAL * 6);
		}

		int stageLength = Math.max(1, Grit.CONFIG.dread.stageSeconds) * 20;
		state.darkTicks = Math.min(state.darkTicks, stageLength * (MAX_STAGE + 1));
		int stage = Math.min(MAX_STAGE, state.darkTicks / stageLength);

		if (stage > state.dreadStage) {
			player.sendMessage(Text.translatable("message.grit.dread." + stage).formatted(Formatting.DARK_GRAY, Formatting.ITALIC), true);
		} else if (stage == 0 && state.dreadStage >= 2) {
			player.sendMessage(Text.translatable("message.grit.dread.relief").formatted(Formatting.GRAY), true);
		}
		state.dreadStage = stage;

		if (!dark || stage == 0) return;

		// 1+: шёпоты пещеры
		if (player.getRandom().nextInt(Math.max(4, 40 - stage * 8)) == 0) {
			play(player, "ambient.cave", 0.6f + stage * 0.1f, 0.8f + player.getRandom().nextFloat() * 0.4f);
		}
		// 2+: сердцебиение, учащается со стадией
		if (stage >= 2) {
			int beat = stage >= 4 ? 20 : stage == 3 ? 30 : 40;
			if (tick % beat < CHECK_INTERVAL) {
				play(player, "entity.warden.heartbeat", 0.5f, 1.0f);
			}
		}
		// 3+: тьма в глазах и слабость
		if (stage >= 3) {
			if (!player.hasStatusEffect(StatusEffects.DARKNESS)) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 140, 0, true, false, false));
			}
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, true, false, true));
		}
		// 4: паника
		if (stage >= 4) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 0, true, false, true));
		}
	}

	private static boolean isInDarkness(ServerPlayerEntity player, ServerWorld world) {
		if (world.getRegistryKey() != World.OVERWORLD) return false;
		if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) return false;
		BlockPos eyes = BlockPos.ofFloored(player.getEyePos());
		if (world.isSkyVisible(eyes)) return false;
		return world.getLightLevel(eyes) <= Grit.CONFIG.dread.darkLightLevel;
	}

	private static void play(ServerPlayerEntity player, String id, float volume, float pitch) {
		SoundEvent sound = Registries.SOUND_EVENT.get(new Identifier("minecraft", id));
		if (sound != null) {
			player.playSound(sound, SoundCategory.AMBIENT, volume, pitch);
		}
	}
}
