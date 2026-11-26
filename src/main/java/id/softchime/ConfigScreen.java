package id.softchime;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Settings panel with two tabs (Sound / Effects). Open with N, or from Options. */
public class ConfigScreen extends Screen {
	private static final int GOLD = 0xFFFFD866;
	private static final int PANEL = 0xF0151826;
	private static final int MUTED = 0xFF9AA3C7;

	private final Screen parent;
	private int tab = 0;
	private int px, py, pw, ph;
	private int tabX, tabY, tabW, tabH;
	private int labelX, labelY;
	private final List<int[]> cardRects = new ArrayList<>(); // x, y, w, h
	private final List<SoundStyle> cardStyles = new ArrayList<>();

	public ConfigScreen(Screen parent) {
		super(Component.literal("Soft Chime UI"));
		this.parent = parent;
	}

	private static void click() { Sounds.play(Sounds.Type.CLICK); }

	private static Component onOff(String label, boolean v) {
		return Component.literal(label + "  ")
			.append(Component.literal(v ? "ON" : "OFF").withStyle(v ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	private Button toggle(int x, int y, int w, int h, String label, Supplier<Boolean> get, Consumer<Boolean> set) {
		return Button.builder(onOff(label, get.get()), b -> {
			set.accept(!get.get());
			b.setMessage(onOff(label, get.get()));
			Config.save();
			click();
		}).bounds(x, y, w, h).build();
	}

	@Override
	protected void init() {
		Config c = Config.INSTANCE;
		cardRects.clear();
		cardStyles.clear();

		boolean compact = this.height < 240;
		int rowH = compact ? 18 : 20;
		int cardH = compact ? 22 : 28;
		pw = Math.min(this.width - 12, 330);
		int innerX = 0, innerW = pw - 20;

		int effectCount = UiFx.REANIMATED ? 8 : 7;
		int content = tab == 0
			? 12 + 2 * cardH + 4 + 10 + rowH + 6 + rowH
			: ((effectCount + 1) / 2) * (rowH + 4);
		ph = 10 + 14 + 8 + 20 + 8 + content + 10 + rowH + 10;
		px = (this.width - pw) / 2;
		py = Math.max(2, (this.height - ph) / 2);
		innerX = px + 10;

		// tabs
		tabX = innerX; tabY = py + 32; tabW = (innerW - 4) / 2; tabH = 20;
		addRenderableWidget(Button.builder(Component.literal("Sound"), b -> { tab = 0; click(); rebuildWidgets(); })
			.bounds(tabX, tabY, tabW, tabH).build());
		addRenderableWidget(Button.builder(Component.literal("Effects"), b -> { tab = 1; click(); rebuildWidgets(); })
			.bounds(tabX + tabW + 4, tabY, tabW, tabH).build());

		int y = tabY + tabH + 8;
		if (tab == 0) {
			labelX = innerX; labelY = y + 1;
			y += 12;
			SoundStyle[] styles = SoundStyle.values();
			int cw = (innerW - 8) / 3;
			for (int i = 0; i < styles.length; i++) {
				SoundStyle st = styles[i];
				int cx = innerX + (i % 3) * (cw + 4);
				int cy = y + (i / 3) * (cardH + 2);
				addRenderableWidget(Button.builder(Component.literal(st.label.replace(" (Default)", "")), b -> {
					c.style = st.name();
					Config.save();
					Sounds.play(st, Sounds.Type.CLICK); // instant preview of the style you picked
				}).bounds(cx, cy, cw, cardH).build());
				cardRects.add(new int[]{cx, cy, cw, cardH});
				cardStyles.add(st);
			}
			y += 2 * cardH + 4 + 10;

			addRenderableWidget(new AbstractSliderButton(innerX, y, innerW, rowH, volText(), c.volume) {
				@Override protected void updateMessage() { setMessage(volText()); }
				@Override protected void applyValue() { c.volume = (float) this.value; Config.save(); }
			});
			y += rowH + 6;

			int pwid = (innerW - 12) / 4;
			String[] names = {"Click", "Open", "Close", "Hover"};
			Sounds.Type[] types = {Sounds.Type.CLICK, Sounds.Type.OPEN, Sounds.Type.CLOSE, Sounds.Type.HOVER};
			for (int i = 0; i < 4; i++) {
				Sounds.Type t = types[i];
				addRenderableWidget(Button.builder(Component.literal("▶ " + names[i]), b -> Sounds.play(t))
					.bounds(innerX + i * (pwid + 4), y, pwid, rowH).build());
			}
		} else {
			int cw = (innerW - 4) / 2;
			List<Button> list = new ArrayList<>();
			list.add(toggle(0, 0, cw, rowH, "Click sound", () -> c.clickSound, v -> c.clickSound = v));
			list.add(toggle(0, 0, cw, rowH, "Open/close sound", () -> c.openCloseSound, v -> c.openCloseSound = v));
			list.add(toggle(0, 0, cw, rowH, "Hover sound", () -> c.hoverSound, v -> c.hoverSound = v));
			list.add(toggle(0, 0, cw, rowH, "Cursor ring", () -> c.cursorRing, v -> c.cursorRing = v));
			list.add(toggle(0, 0, cw, rowH, "Click effect", () -> c.clickParticles, v -> c.clickParticles = v));
			list.add(toggle(0, 0, cw, rowH, "Animations", () -> c.animations, v -> c.animations = v));
			if (UiFx.REANIMATED)
				list.add(toggle(0, 0, cw, rowH, "ReAnimated compat", () -> c.reanimatedCompat, v -> c.reanimatedCompat = v));
			list.add(toggle(0, 0, cw, rowH, "Options button", () -> c.optionsButton, v -> c.optionsButton = v));
			for (int i = 0; i < list.size(); i++) {
				Button b = list.get(i);
				b.setX(innerX + (i % 2) * (cw + 4));
				b.setY(y + (i / 2) * (rowH + 4));
				addRenderableWidget(b);
			}
		}

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> { click(); onClose(); })
			.bounds(innerX, py + ph - 10 - rowH, innerW, rowH).build());
	}

	private Component volText() { return Component.literal("Volume: " + Math.round(Config.INSTANCE.volume * 100) + "%"); }

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
		extractTransparentBackground(g);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
		// panel
		g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0x66000000);
		g.fill(px, py, px + pw, py + ph, PANEL);
		g.fillGradient(px, py, px + pw, py + 30, 0xFF2C3150, PANEL);
		g.fill(px, py, px + pw, py + 2, GOLD);
		g.outline(px, py, pw, ph, 0x66FFD866);

		// title
		g.centeredText(getFont(), Component.literal("Soft Chime UI").withStyle(ChatFormatting.BOLD), px + pw / 2, py + 9, GOLD);

		super.extractRenderState(g, mx, my, delta); // buttons, slider

		// active tab underline
		int ux = tab == 0 ? tabX : tabX + tabW + 4;
		g.fill(ux, tabY + tabH, ux + tabW, tabY + tabH + 2, GOLD);

		if (tab == 0) {
			g.text(getFont(), "SOUND STYLE  (tap to preview)", labelX, labelY, MUTED);
			SoundStyle cur = Config.INSTANCE.style();
			for (int i = 0; i < cardRects.size(); i++) {
				if (cardStyles.get(i) == cur) {
					int[] r = cardRects.get(i);
					g.outline(r[0] - 1, r[1] - 1, r[2] + 2, r[3] + 2, GOLD);
					g.outline(r[0], r[1], r[2], r[3], 0xFFFFFFFF);
				}
			}
		}
	}

	@Override
	public void onClose() {
		Config.save();
		Minecraft.getInstance().gui.setScreen(parent);
	}
}
