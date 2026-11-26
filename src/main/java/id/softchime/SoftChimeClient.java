package id.softchime;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftChimeClient implements ClientModInitializer {
	public static final String MOD_ID = "softchime";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyMapping openConfigKey;
	private Screen lastScreen;

	@Override
	public void onInitializeClient() {
		Config.load();

		// Press K (changeable in Controls) to open the sound-style menu.
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		openConfigKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.softchime.config", InputConstants.Type.KEYBOARD, InputConstants.KEY_K, category));

		// Open/close sounds for every screen: inventory, chest, furnace, pause menu, creative tabs, ...
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openConfigKey.consumeClick()) {
				mc.setScreen(new ConfigScreen(mc.screen));
			}
			Screen now = mc.screen;
			if (now != lastScreen) {
				if (Config.INSTANCE.openCloseSound) {
					if (now != null) Sounds.play(Sounds.Type.OPEN);
					else if (lastScreen != null) Sounds.play(Sounds.Type.CLOSE);
				}
				if (now != null) UiFx.onScreenOpened();
				lastScreen = now;
			}
		});

		// Draw cursor ring / ripples / particles / hover glow on top of every screen.
		ScreenEvents.AFTER_INIT.register((client, screen, w, h) ->
			ScreenEvents.afterExtract(screen).register(UiFx::render));
	}
}
