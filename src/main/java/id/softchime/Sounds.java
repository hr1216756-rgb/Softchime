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
	public enum Type { CLICK, OPEN, CLOSE, HOVER, TICK, SLIDE }

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
		play(style, type, 0.97f + (float) Math.random() * 0.06f); // tiny variation, feels alive
	}

	/** Slider sound: each style has its own sample, pitch follows the slider position (0..1). */
	public static void slide(float position) {
		play(Config.INSTANCE.style(), Type.SLIDE, 0.8f + Math.max(0f, Math.min(1f, position)) * 0.7f);
	}

	public static void play(SoundStyle style, Type type, float pitch) {
		float vol = Config.INSTANCE.volume;
		if (vol <= 0f) return;
		Minecraft.getInstance().getSoundManager()
			.play(SimpleSoundInstance.forUI(get(style, type), pitch, vol));
	}

	private Sounds() {}
}
