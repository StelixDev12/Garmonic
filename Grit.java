package dev.grit;

import dev.grit.mechanic.BurdenHandler;
import dev.grit.mechanic.DreadHandler;
import dev.grit.mechanic.FullMoonHandler;
import dev.grit.mechanic.PlayerState;
import dev.grit.mechanic.WoundHandler;
import dev.grit.registry.ModEffects;
import dev.grit.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grit — суровое выживание в духе ванили.
 * Всё работает на сервере; клиенту мод нужен только ради шины, эффекта перелома и текстур.
 *
 * Мод сознательно не трогает температуру, жажду, еду и сезоны, чтобы спокойно жить
 * рядом с Tough As Nails, Serene Seasons и Farmer's Delight. Миксинов нет.
 */
public class Grit implements ModInitializer {
	public static final String MOD_ID = "grit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static GritConfig CONFIG = new GritConfig();

	@Override
	public void onInitialize() {
		CONFIG = GritConfig.load();

		ModEffects.register();
		ModItems.register();

		ServerLivingEntityEvents.ALLOW_DAMAGE.register(WoundHandler::onDamage);
		ServerEntityEvents.ENTITY_LOAD.register(FullMoonHandler::onEntityLoad);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerState.remove(handler.player));

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int tick = server.getTicks();
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
					continue;
				}
				PlayerState state = PlayerState.of(player);
				if (CONFIG.dread.enabled) DreadHandler.tick(player, state, tick);
				if (CONFIG.burden.enabled) BurdenHandler.tick(player, state, tick);
			}
			if (CONFIG.fullMoon.enabled) FullMoonHandler.tick(server);
		});

		LOGGER.info("Grit loaded. Survival just got harsher.");
	}
}
