package id.softchime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Simple JSON config: .minecraft/config/softchime.json */
public class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("softchime.json");
	public static Config INSTANCE = new Config();

	public String style = SoundStyle.SOFT_CHIME.name();
	public float volume = 0.8f;
	public boolean clickSound = true;
	public boolean openCloseSound = true;
	public boolean hoverSound = true;
	public boolean cursorRing = true;
	public boolean clickParticles = true;
	public boolean animations = true;
	/** When ReAnimated is installed: let it handle open/hover animations, we keep sounds + cursor + particles. */
	public boolean reanimatedCompat = true;
	public boolean optionsButton = true;

	public SoundStyle style() {
		try { return SoundStyle.valueOf(style); } catch (Exception e) { return SoundStyle.SOFT_CHIME; }
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				Config c = GSON.fromJson(Files.readString(FILE), Config.class);
				if (c != null) INSTANCE = c;
			}
		} catch (Exception e) { SoftChimeClient.LOGGER.warn("Could not read config", e); }
		save();
	}

	public static void save() {
		try { Files.writeString(FILE, GSON.toJson(INSTANCE)); }
		catch (Exception e) { SoftChimeClient.LOGGER.warn("Could not save config", e); }
	}
}
