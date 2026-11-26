package id.softchime;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
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

		// Press N (changeable in Controls) to open the sound-style menu.
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		openConfigKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.softchime.config", InputConstants.Type.KEYBOARD, InputConstants.KEY_N, category));

		// Open/close sounds for every screen: inventory, chest, furnace, pause menu, creative tabs, ...
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openConfigKey.consumeClick()) {
				mc.gui.setScreen(new ConfigScreen(mc.gui.screen()));
			}
			Screen now = mc.gui.screen();
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
		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			ScreenEvents.afterExtract(screen).register(UiFx::render);
			ScreenMouseEvents.beforeMouseClick(screen).register((scr, event) -> UiFx.onClick(scr, event.x(), event.y(), event.button()));
			// Settings button inside Options (handy on phones where the K key is awkward).
			if (Config.INSTANCE.optionsButton && screen.getClass().getSimpleName().equals("OptionsScreen")) {
				try {
					Button b = Button.builder(Component.literal("Soft Chime"),
						btn -> client.gui.setScreen(new ConfigScreen(screen)))
						.bounds(6, h / 2 - 10, 90, 20).build();
					Screens.getWidgets(screen).add(b);
				} catch (Throwable t) { LOGGER.warn("Could not add options button", t); }
			}
		});
	}
}
