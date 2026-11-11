package dev.grit.mechanic;

import dev.grit.Grit;
import dev.grit.GritConfig;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * Тяжёлая ноша. Руда, блоки металлов, наковальни и обсидиан тяжёлые (#grit:heavy),
 * камень и прочий строительный булыжник громоздкий (#grit:bulky).
 * Перегруз замедляет, а сильный перегруз ещё и быстрее изматывает при беге.
 * Шалкеры и эндер-сундук — честное решение.
 */
public final class BurdenHandler {
	public static final TagKey<Item> HEAVY = TagKey.of(RegistryKeys.ITEM, new Identifier(Grit.MOD_ID, "heavy"));
	public static final TagKey<Item> BULKY = TagKey.of(RegistryKeys.ITEM, new Identifier(Grit.MOD_ID, "bulky"));

	private static final UUID MODIFIER_ID = UUID.fromString("0e8b3a52-71d4-4f8c-a1b6-93c2d7e4f5a1");
	private static final double[] SLOWDOWN = {0.0, -0.12, -0.25};

	private BurdenHandler() {}

	public static void tick(ServerPlayerEntity player, PlayerState state, int tick) {
		if (state.burdenTier == 2 && player.isSprinting()) {
			player.addExhaustion(0.01f);
		}
		if ((tick + player.getId()) % 20 != 5) return;

		GritConfig.Burden cfg = Grit.CONFIG.burden;
		double weight = weigh(player.getInventory(), cfg);
		int tier = weight >= cfg.tier2Weight ? 2 : weight >= cfg.tier1Weight ? 1 : 0;

		EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed == null) return;

		boolean hasModifier = speed.getModifier(MODIFIER_ID) != null;
		if (tier == state.burdenTier && hasModifier == (tier > 0)) return;

		if (hasModifier) speed.removeModifier(MODIFIER_ID);
		if (tier > 0) {
			speed.addTemporaryModifier(new EntityAttributeModifier(MODIFIER_ID, "Grit burden",
					SLOWDOWN[tier], EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
		}
		if (tier != state.burdenTier) {
			String key = tier > state.burdenTier ? "message.grit.burden." + tier : "message.grit.burden.lighter";
			player.sendMessage(Text.translatable(key).formatted(tier > state.burdenTier ? Formatting.YELLOW : Formatting.GRAY), true);
		}
		state.burdenTier = tier;
	}

	private static double weigh(PlayerInventory inventory, GritConfig.Burden cfg) {
		double weight = 0;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isEmpty()) continue;
			if (stack.isIn(HEAVY)) {
				weight += stack.getCount();
			} else if (stack.isIn(BULKY)) {
				weight += stack.getCount() * cfg.bulkyWeight;
			}
		}
		return weight;
	}
}
