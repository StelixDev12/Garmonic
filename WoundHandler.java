package dev.grit.mechanic;

import dev.grit.Grit;
import dev.grit.GritConfig;
import dev.grit.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Переломы: тяжёлое падение может сломать ногу. */
public final class WoundHandler {
	private WoundHandler() {}

	public static boolean onDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player) || player.isCreative() || player.isSpectator()) {
			return true;
		}
		GritConfig.Wounds cfg = Grit.CONFIG.wounds;
		if (cfg.fractures && source.isIn(DamageTypeTags.IS_FALL)) {
			tryFracture(player, amount, cfg);
		}
		return true; // урон никогда не отменяем, только добавляем последствия
	}

	private static void tryFracture(ServerPlayerEntity player, float amount, GritConfig.Wounds cfg) {
		float min = cfg.fractureMinFallDamage;
		if (amount < min) return;

		double chance = amount >= min + 4 ? 1.0 : 0.4 + (amount - min) * 0.15;
		if (player.getRandom().nextDouble() >= chance) return;

		StatusEffectInstance current = player.getStatusEffect(ModEffects.FRACTURE);
		int amplifier = current == null ? 0 : 1;
		player.addStatusEffect(new StatusEffectInstance(ModEffects.FRACTURE, 20 * cfg.fractureSeconds, amplifier, false, true, true));
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.PLAYERS, 1.0f, 0.6f);
		player.sendMessage(Text.translatable("message.grit.fracture").formatted(Formatting.RED), true);
	}
}
