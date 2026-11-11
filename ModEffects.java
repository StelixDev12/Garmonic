package dev.grit.registry;

import dev.grit.Grit;
import dev.grit.effect.FractureEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEffects {
	public static final StatusEffect FRACTURE = new FractureEffect();

	private ModEffects() {}

	public static void register() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier(Grit.MOD_ID, "fracture"), FRACTURE);
	}
}
