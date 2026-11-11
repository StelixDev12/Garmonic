package dev.grit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Конфиг лежит в config/grit.json. Создаётся при первом запуске,
 * новые поля дописываются автоматически при обновлении мода.
 */
public class GritConfig {
	public Wounds wounds = new Wounds();
	public Dread dread = new Dread();
	public Burden burden = new Burden();
	public FullMoon fullMoon = new FullMoon();

	public static class Wounds {
		public boolean fractures = true;
		/** Урон от падения (до брони и «Невесомости»), с которого возможен перелом. */
		public float fractureMinFallDamage = 6.0f;
		/** Длительность перелома в секундах без шины. */
		public int fractureSeconds = 180;
	}

	public static class Dread {
		public boolean enabled = true;
		/** Через сколько секунд в полной темноте начинаются шёпоты. Дальше стадии идут с тем же шагом. */
		public int stageSeconds = 45;
		/** Максимальный уровень света, который считается «темнотой». */
		public int darkLightLevel = 2;
	}

	public static class Burden {
		public boolean enabled = true;
		/** Вес, с которого начинается замедление (1 «тяжёлый» предмет = 1, 1 «громоздкий» = bulkyWeight). */
		public double tier1Weight = 192;
		public double tier2Weight = 384;
		public double bulkyWeight = 0.15;
	}

	public static class FullMoon {
		public boolean enabled = true;
		public double healthBonus = 0.3;
		public double speedBonus = 0.1;
		public double damageBonus = 0.2;
		public double followRangeBonus = 0.5;
		public boolean announce = true;
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static GritConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("grit.json");
		GritConfig config = new GritConfig();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				GritConfig read = GSON.fromJson(reader, GritConfig.class);
				if (read != null) config = read;
			} catch (Exception e) {
				Grit.LOGGER.error("Failed to read config/grit.json, using defaults", e);
				return config;
			}
		}
		config.fillMissing();
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			GSON.toJson(config, writer);
		} catch (Exception e) {
			Grit.LOGGER.error("Failed to write config/grit.json", e);
		}
		return config;
	}

	private void fillMissing() {
		if (wounds == null) wounds = new Wounds();
		if (dread == null) dread = new Dread();
		if (burden == null) burden = new Burden();
		if (fullMoon == null) fullMoon = new FullMoon();
	}
}
