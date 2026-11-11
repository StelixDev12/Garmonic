package dev.grit.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Перелом: -25% скорости за уровень и чуть слабее удар.
 * Сам проходит со временем; шина сильно ускоряет заживление.
 */
public class FractureEffect extends StatusEffect {
	public FractureEffect() {
		super(StatusEffectCategory.HARMFUL, 0xD8CFB0);
		addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,
				"5b3c1f0e-9d1a-4c7e-8f2b-6a4d3e2c1b0a", -0.25, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
		addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED,
				"c2e8a7d4-3f61-4b59-9e0d-1a7b6c5d4e3f", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	}
}
