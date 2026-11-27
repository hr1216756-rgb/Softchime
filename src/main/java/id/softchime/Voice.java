package id.softchime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional voice lines ("oh, you opened your inventory"). The mod ships NO voice recordings:
 * put your own .ogg files at assets/softchime/sounds/voice_<key>.ogg (e.g. via a resource pack)
 * and they play automatically. If a file is missing, nothing happens.
 * Keys: inventory, chest, furnace, crafting, pause, settings.
 */
public final class Voice {
	private static String pending;
	private static int delay;
	private static final Map<String, Long> LAST = new HashMap<>();

	public static String keyFor(Screen s) {
		if (s == null) return null;
		if (s instanceof ConfigScreen) return "settings";
		String n = s.getClass().getSimpleName();
		if (n.contains("Furnace") || n.contains("Smoker")) return "furnace";
		if (n.contains("Crafting")) return "crafting";
		if (n.contains("Inventory")) return "inventory";
		if (n.contains("Shulker") || n.contains("Container") || n.contains("Chest") || n.contains("Generic")) return "chest";
		if (n.contains("Pause")) return "pause";
		return null;
	}

	/** Called when a new screen opens. The line starts a moment after the UI sound so they don't overlap. */
	public static void onOpen(Screen s) {
		if (!Config.INSTANCE.voiceLines) return;
		String key = keyFor(s);
		if (key == null) return;
		pending = key;
		delay = 5; // ticks
	}

	public static void tick() {
		if (pending == null || --delay > 0) return;
		String key = pending;
		pending = null;
		play(key);
	}

	private static void play(String key) {
		long now = System.currentTimeMillis();
		Long last = LAST.get(key);
		if (last != null && now - last < 2500) return; // no spam when opening/closing quickly
		Minecraft mc = Minecraft.getInstance();
		Identifier file = Identifier.fromNamespaceAndPath(SoftChimeClient.MOD_ID, "sounds/voice_" + key + ".ogg");
		if (mc.getResourceManager().getResource(file).isEmpty()) return; // no recording installed -> stay silent
		LAST.put(key, now);
		SoundEvent ev = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(SoftChimeClient.MOD_ID, "voice_" + key));
		mc.getSoundManager().play(SimpleSoundInstance.forUI(ev, 1.0f, Math.max(0.05f, Config.INSTANCE.volume)));
	}

	private Voice() {}
}
