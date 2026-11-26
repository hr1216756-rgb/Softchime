package id.softchime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Sound events are created WITHOUT registering them in the registry on purpose:
 * this is a client-only mod, so nothing has to be synced with servers.
 */
public final class Sounds {
	public enum Type { CLICK, OPEN, CLOSE, HOVER, TICK }

	private static final Map<String, SoundEvent> CACHE = new HashMap<>();

	private static SoundEvent get(SoundStyle style, Type type) {
		String name = style.id + "_" + type.name().toLowerCase();
		return CACHE.computeIfAbsent(name, n -> {
			Identifier id = Identifier.fromNamespaceAndPath(SoftChimeClient.MOD_ID, n);
			return SoundEvent.createVariableRangeEvent(id);
		});
	}

	public static void play(Type type) { play(Config.INSTANCE.style(), type); }

	public static void play(SoundStyle style, Type type) {
		float vol = Config.INSTANCE.volume;
		if (vol <= 0f) return;
		float pitch = 0.97f + (float) Math.random() * 0.06f; // tiny variation, feels alive
		Minecraft.getInstance().getSoundManager()
			.play(SimpleSoundInstance.forUI(get(style, type), pitch, vol));
	}

	private Sounds() {}
}
