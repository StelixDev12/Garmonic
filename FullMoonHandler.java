package dev.grit.mechanic;

import dev.grit.Grit;
import dev.grit.GritConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Полнолуние. Каждую 8-ю ночь (ванильная фаза луны 0) враждебные мобы, появившиеся
 * в эту ночь, крепче, быстрее, больнее бьют и дальше чуют игрока.
 * На закате воют волки и игроков предупреждают.
 */
public final class FullMoonHandler {
	public static final String TAG = "grit_full_moon";

	private static final UUID HEALTH_ID = UUID.fromString("a4f1c6e2-2b7d-4e93-8c5a-1d0f9b3e7c61");
	private static final UUID SPEED_ID = UUID.fromString("b7e2d9f3-6c1a-4f05-9b8e-2e4a0c6d8f72");
	private static final UUID DAMAGE_ID = UUID.fromString("c3a8e1b5-9f2d-4a76-8e1c-3f5b1d7a9e83");
	private static final UUID FOLLOW_ID = UUID.fromString("d9b4f2c6-1e3a-4b87-9f2d-4a6c2e8b0f94");

	private static long announcedDay = -1;

	private FullMoonHandler() {}

	public static boolean isFullMoonNight(World world) {
		if (world.getRegistryKey() != World.OVERWORLD || !world.isNight()) return false;
		long day = world.getTimeOfDay() / 24000L;
		return day % 8L == 0L; // ванильная фаза 0 — полная луна
	}

	public static void onEntityLoad(Entity entity, ServerWorld world) {
		if (!(entity instanceof HostileEntity mob)) return;
		if (mob.getCommandTags().contains(TAG)) return;
		if (!isFullMoonNight(world)) return;

		mob.addCommandTag(TAG);
		GritConfig.FullMoon cfg = Grit.CONFIG.fullMoon;
		boolean healthAdded = boost(mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH), HEALTH_ID, cfg.healthBonus);
		boost(mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED), SPEED_ID, cfg.speedBonus);
		boost(mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE), DAMAGE_ID, cfg.damageBonus);
		boost(mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE), FOLLOW_ID, cfg.followRangeBonus);
		if (healthAdded) {
			mob.setHealth(mob.getMaxHealth());
		}
	}

	private static boolean boost(EntityAttributeInstance instance, UUID id, double amount) {
		if (instance == null || amount == 0 || instance.getModifier(id) != null) return false;
		EntityAttribute attribute = instance.getAttribute();
		instance.addPersistentModifier(new EntityAttributeModifier(id, "Grit full moon: " + attribute.getTranslationKey(),
				amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
		return true;
	}

	public static void tick(MinecraftServer server) {
		if (!Grit.CONFIG.fullMoon.announce) return;
		ServerWorld overworld = server.getOverworld();
		if (overworld == null || !isFullMoonNight(overworld)) return;

		long day = overworld.getTimeOfDay() / 24000L;
		if (day == announcedDay) return;
		announcedDay = day;

		for (ServerPlayerEntity player : overworld.getPlayers()) {
			player.sendMessage(Text.translatable("message.grit.full_moon").formatted(Formatting.GOLD, Formatting.ITALIC), true);
			SoundEvent howl = Registries.SOUND_EVENT.get(new Identifier("minecraft", "entity.wolf.howl"));
			if (howl != null) player.playSound(howl, SoundCategory.AMBIENT, 0.7f, 0.9f);
		}
	}
}
