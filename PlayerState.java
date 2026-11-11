package dev.grit.mechanic;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Временное состояние игрока (не сохраняется: всё пересчитывается за секунду после входа). */
public final class PlayerState {
	private static final Map<UUID, PlayerState> STATES = new HashMap<>();

	// Темнота
	public int darkTicks;
	public int dreadStage;

	// Ноша
	public int burdenTier;

	private PlayerState() {}

	public static PlayerState of(PlayerEntity player) {
		return STATES.computeIfAbsent(player.getUuid(), id -> new PlayerState());
	}

	public static void remove(PlayerEntity player) {
		STATES.remove(player.getUuid());
	}
}
