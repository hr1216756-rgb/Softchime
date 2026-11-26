package id.softchime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/** In-game menu: choose the sound style, preview it, volume + toggles. Open with K. */
public class ConfigScreen extends Screen {
	private final Screen parent;
	private Button styleButton;

	public ConfigScreen(Screen parent) {
		super(Component.literal("Soft Chime UI"));
		this.parent = parent;
	}

	private static Component onOff(String label, boolean v) {
		return Component.literal(label + ": " + (v ? "ON" : "OFF"));
	}

	private Button toggle(int x, int y, int w, String label, Supplier<Boolean> get, java.util.function.Consumer<Boolean> set) {
		Button[] ref = new Button[1];
		ref[0] = Button.builder(onOff(label, get.get()), b -> {
			set.accept(!get.get());
			b.setMessage(onOff(label, get.get()));
			Config.save();
		}).bounds(x, y, w, 20).build();
		return ref[0];
	}

	@Override
	protected void init() {
		Config c = Config.INSTANCE;
		int w = 240, x = this.width / 2 - w / 2, y = 30;

		// header (disabled button used as a label)
		Button header = Button.builder(Component.literal("UI SETTINGS - Sound style for the whole menu"), b -> {})
			.bounds(x - 40, y, w + 80, 20).build();
		header.active = false;
		addRenderableWidget(header);
		y += 28;

		styleButton = Button.builder(styleText(), b -> {
			c.style = c.style().next().name();
			b.setMessage(styleText());
			Config.save();
			Sounds.play(Sounds.Type.CLICK);
		}).bounds(x, y, w, 20).build();
		addRenderableWidget(styleButton);
		y += 24;

		addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
			c.style = c.style().prev().name();
			styleButton.setMessage(styleText());
			Config.save();
			Sounds.play(Sounds.Type.CLICK);
		}).bounds(x, y, w / 2 - 2, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
			c.style = c.style().next().name();
			styleButton.setMessage(styleText());
			Config.save();
			Sounds.play(Sounds.Type.CLICK);
		}).bounds(x + w / 2 + 2, y, w / 2 - 2, 20).build());
		y += 24;

		addRenderableWidget(Button.builder(Component.literal("Preview sound (Click / Open / Close)"), b -> {
			Sounds.play(Sounds.Type.CLICK);
			Minecraft.getInstance().schedule(() -> {});
			new Thread(() -> {
				try { Thread.sleep(260); Minecraft.getInstance().execute(() -> Sounds.play(Sounds.Type.OPEN));
					Thread.sleep(420); Minecraft.getInstance().execute(() -> Sounds.play(Sounds.Type.CLOSE)); }
				catch (InterruptedException ignored) {}
			}).start();
		}).bounds(x, y, w, 20).build());
		y += 24;

		addRenderableWidget(new AbstractSliderButton(x, y, w, 20, volText(), c.volume) {
			@Override protected void updateMessage() { setMessage(volText()); }
			@Override protected void applyValue() { c.volume = (float) this.value; Config.save(); }
		});
		y += 28;

		addRenderableWidget(toggle(x, y, w, "Click sound", () -> c.clickSound, v -> c.clickSound = v)); y += 22;
		addRenderableWidget(toggle(x, y, w, "Open / close sound", () -> c.openCloseSound, v -> c.openCloseSound = v)); y += 22;
		addRenderableWidget(toggle(x, y, w, "Hover sound", () -> c.hoverSound, v -> c.hoverSound = v)); y += 22;
		addRenderableWidget(toggle(x, y, w, "Cursor ring", () -> c.cursorRing, v -> c.cursorRing = v)); y += 22;
		addRenderableWidget(toggle(x, y, w, "Click particles", () -> c.clickParticles, v -> c.clickParticles = v)); y += 22;
		addRenderableWidget(toggle(x, y, w, "Animations", () -> c.animations, v -> c.animations = v)); y += 28;

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(x, y, w, 20).build());
	}

	private Component styleText() { return Component.literal("UI Sound: " + Config.INSTANCE.style().label); }
	private Component volText() { return Component.literal("Volume: " + Math.round(Config.INSTANCE.volume * 100) + "%"); }

	@Override
	public void onClose() {
		Config.save();
		Minecraft.getInstance().setScreen(parent);
	}
}
