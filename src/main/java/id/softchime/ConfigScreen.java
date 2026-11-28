package id.softchime;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.input.KeyEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Settings screen: title bar, sidebar with categories, and a list of option rows (Sound / Effects / Theme / About). Open with N. */
public class ConfigScreen extends Screen {
	// themes: CARD, CARD2, CARD3, DEEP, HDR, A1, A2, A3, MUTED, DARK, SEL1, SEL2, TITLE-SHADOW
	private static final String[] THEME_NAMES = {"Night", "Sakura", "Ocean", "Forest", "Sunset", "Graphite"};
	private static final int[][] THEMES = {
		{0xFF221C45, 0xFF2D2659, 0xFF3A3170, 0xFF1A1538, 0xFF2E2766, 0xFF6BF5D0, 0xFFFFD866, 0xFFFF5FA2, 0xFFA79FD6, 0xFF2B2455, 0xFF3B3470, 0xFF46397F, 0xFFB03A78},
		{0xFF3A1A2E, 0xFF4F2640, 0xFF66324F, 0xFF2A1121, 0xFF5A2A47, 0xFFFFB7D5, 0xFFFFE3A3, 0xFFFF6FA8, 0xFFD9A3BF, 0xFF3A1A2E, 0xFF6A3454, 0xFF7A3F61, 0xFF8A2A5C},
		{0xFF10243F, 0xFF16345A, 0xFF1F4673, 0xFF0B1A30, 0xFF1B3F6B, 0xFF5CE1E6, 0xFF9AD1FF, 0xFF7C8CFF, 0xFF8FB4D9, 0xFF10243F, 0xFF20507F, 0xFF2A5E92, 0xFF1E5AA0},
		{0xFF14301F, 0xFF1C4029, 0xFF285636, 0xFF0E2216, 0xFF215A33, 0xFF7DFF8A, 0xFFE8F29A, 0xFFFFB86B, 0xFF9CC9A8, 0xFF14301F, 0xFF2C6240, 0xFF367551, 0xFF1F7A3A},
		{0xFF2F1B3F, 0xFF43255A, 0xFF5A3175, 0xFF220F30, 0xFF4A2A66, 0xFFFFB36B, 0xFFFFE066, 0xFFFF6B8B, 0xFFC3A3DE, 0xFF2F1B3F, 0xFF63367F, 0xFF743F93, 0xFFA03A5A},
		{0xFF1E1F26, 0xFF2A2C36, 0xFF383B48, 0xFF15161B, 0xFF2B2D3A, 0xFFE6E9F5, 0xFFFFE066, 0xFF9AA5FF, 0xFF9EA3B5, 0xFF1E1F26, 0xFF3A3D4C, 0xFF464A5C, 0xFF4A4F8A},
		{0xFF353535, 0xFFD2D2D2, 0xFFF0F0F0, 0xFF222222, 0xFFE4E4E4, 0xFFF0A93B, 0xFFF0A93B, 0xFFE0902A, 0xFFBDBDBD, 0xFF161616, 0xFFF0A93B, 0xFFF6BC5E, 0xFF000000},
	};
	private static int CARD, CARD2, CARD3, DEEP, HDR, MINT, YEL, PINK, MUTED, DARK, SEL1, SEL2, TSH;
	private static int TXT = 0xFFFFFFFF, MUTED2, TC, HC, SIDESEL;
	private static boolean flat; // "Action" skin: square corners, grey panels, light buttons
	private static final int WHITE = 0xFFFFFFFF;
	private static final int OFF = 0xFF4A4378;

	public static void applyTheme(int i) {
		int[] t = THEMES[Math.max(0, Math.min(THEMES.length - 1, i))];
		CARD = t[0]; CARD2 = t[1]; CARD3 = t[2]; DEEP = t[3]; HDR = t[4]; MINT = t[5]; YEL = t[6]; PINK = t[7];
		MUTED = t[8]; DARK = t[9]; SEL1 = t[10]; SEL2 = t[11]; TSH = t[12];
		flat = i == 6;
		TXT = flat ? 0xFF161616 : 0xFFFFFFFF; MUTED2 = flat ? 0xFF5A5A5A : MUTED;
		TC = flat ? 0xFF161616 : YEL; HC = flat ? 0xFFFFFFFF : YEL; SIDESEL = flat ? 0xFF4A4A4A : CARD3;
		UiFx.PALETTE = new int[] {YEL & 0xFFFFFF, 0xFFFFFF, MINT & 0xFFFFFF, PINK & 0xFFFFFF};
	}
	static { applyTheme(0); }


	private static final String[] NAMES = {"Chime", "Bubble", "Marimba", "Crystal", "Retro", "Tap", "Music Box", "Drop", "Harp", "Kalimba", "Shimmer", "Piano",
		"Zen Bowl", "Guitar", "Celesta", "Vibes", "Wood", "Pad"};
	private static final int[] ACCENT = {0xFFFFD866, 0xFF6BD6FF, 0xFFE8903A, 0xFF6BF5D0, 0xFF7DFF8A, 0xFFFF5FA2, 0xFFB48CFF, 0xFF5CE1E6, 0xFFF2D27A, 0xFFD9A066, 0xFFB8F5FF, 0xFFE6E6F0,
		0xFFFFB84D, 0xFFFF8FB1, 0xFF8FF0D8, 0xFF7FB5FF, 0xFFC98A52, 0xFFC7B2FF};
	private static final int NS = SoundStyle.values().length;
	private static final String[] TAB_NAMES = {"Sound", "Effects", "Theme", "About"};
	private static final String[] TAB_SUB = {"Sounds for clicks, menus and sliders", "Switch each effect on or off", "Pick the colours of this menu", "About this mod"};
	private static final SoundStyle[] TAB_ICON = {SoundStyle.MUSIC_BOX, SoundStyle.STAR_SHIMMER, SoundStyle.WATER_DROP, SoundStyle.SOFT_CHIME};

	private record Hit(int x, int y, int w, int h, int id) {
		boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
	}
	private record Toggle(String label, String desc, Supplier<Boolean> get, Consumer<Boolean> set) {}
	/** kind: 0 header, 1 style chips, 2 volume slider, 3 preview buttons, 4 toggle, 5 theme chips, 6 text line */
	private record Item(int kind, int a, String s1, String s2, int h) {}

	private static final Identifier BANNER = Identifier.fromNamespaceAndPath("softchime", "textures/gui/banner.png");
	private final Screen parent;
	private int tab = 0;
	private float scrollPx = 0, scrollTarget = 0;
	private boolean dragScroll = false;
	private int sbTop, sbH, sbThumb, sbMax;
	private double lastMy = -1;
	private long lastMoveYNs = 0;
	private final List<Hit> hits = new ArrayList<>();
	private final List<Toggle> toggles = new ArrayList<>();
	private final Map<Integer, Float> hov = new HashMap<>();
	private int sx, sy, sw; // volume track (from the last drawn frame)
	private boolean dragging;
	private int lastTickStep = -1;
	private double lastMx = -1;
	private long lastMoveNs = 0;
	private final float[] pulse = new float[24];
	private final float[] swT = new float[12];
	private int hoverId = -1;
	private long lastActionNs = 0;
	private int lastActionId = -1;
	private int pressId = -1;
	private long pressNs = 0;
	private float selY = -1, volShown = -1;
	private boolean wasDown = false;
	private long tabStartNs = System.nanoTime();
	private int pw, ph, px, py;
	private static final String VERSION = FabricLoader.getInstance().getModContainer("softchime")
		.map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?");
	private final long startNs = System.nanoTime();
	private long lastNs = startNs;

	public ConfigScreen(Screen parent) {
		super(Component.literal("Soft Chime UI"));
		this.parent = parent;
		applyTheme(Config.INSTANCE.skin ? 6 : Config.INSTANCE.theme);
	}

	@Override
	protected void init() {
		Config c = Config.INSTANCE;
		toggles.clear();
		toggles.add(new Toggle("Click sound", "Sound on every click", () -> c.clickSound, v -> c.clickSound = v));
		toggles.add(new Toggle("Open / close sound", "When a screen opens or closes", () -> c.openCloseSound, v -> c.openCloseSound = v));
		toggles.add(new Toggle("Hover sound", "Soft tick over buttons", () -> c.hoverSound, v -> c.hoverSound = v));
		toggles.add(new Toggle("Cursor ring", "Ring and sparkle trail on the pointer", () -> c.cursorRing, v -> c.cursorRing = v));
		toggles.add(new Toggle("Click effect", "Ripple and sparkles when you click", () -> c.clickParticles, v -> c.clickParticles = v));
		toggles.add(new Toggle("Animations", "Light sweep, shine and glow", () -> c.animations, v -> c.animations = v));
		if (UiFx.REANIMATED) toggles.add(new Toggle("ReAnimated compat", "Do not double ReAnimated effects", () -> c.reanimatedCompat, v -> c.reanimatedCompat = v));
		toggles.add(new Toggle("Options button", "Soft Chime button in Options", () -> c.optionsButton, v -> c.optionsButton = v));
		for (int i = 0; i < toggles.size(); i++) swT[i] = toggles.get(i).get().get() ? 1f : 0f;
	}

	private List<Item> items() {
		List<Item> l = new ArrayList<>();
		if (tab == 0) {
			l.add(new Item(0, SoundStyle.CRYSTAL_GLASS.ordinal(), "Volume", "Drag, or use + and -", flat ? 19 : 22));
			l.add(new Item(2, 0, "", "", 24));
			l.add(new Item(3, 0, "", "", 20));
			l.add(new Item(0, SoundStyle.SOFT_CHIME.ordinal(), "Sound Style", "Pick the sound family", flat ? 19 : 22));
			for (int r = 0; r < (NS + 3) / 4; r++) l.add(new Item(1, r, "", "", flat ? 18 : 22));
		} else if (tab == 1) {
			for (int i = 0; i < toggles.size(); i++) l.add(new Item(4, i, "", "", flat ? 20 : 26));
		} else if (tab == 2) {
			l.add(new Item(0, SoundStyle.WATER_DROP.ordinal(), "Colour Theme", "Changes the whole menu", flat ? 19 : 22));
			for (int r = 0; r < 2; r++) l.add(new Item(5, r, "", "", 24));
		} else {
			l.add(new Item(0, SoundStyle.STAR_SHIMMER.ordinal(), "Secret Code", "Use the keys below (or your keyboard)", flat ? 19 : 22));
			l.add(new Item(7, 0, "", "", 24));
			l.add(new Item(8, 0, "", "", 4 * 16 + 2));
			l.add(new Item(6, 0, "Soft Chime UI  v" + VERSION, "", 14));
			l.add(new Item(6, 1, "Soft UI sounds for every screen.", "", 12));
			l.add(new Item(6, 2, "Click ripples, sparkles and glow.", "", 12));
			l.add(new Item(6, 3, NS + " sound styles, 6 colour themes.", "", 12));
			l.add(new Item(6, 4, "Press N anywhere to open this menu.", "", 12));
		}
		return l;
	}

	// ---------------------------------------------------------------- input
	private static void click() { Sounds.play(Sounds.Type.CLICK); }

	private int hitAt(double mx, double my) {
		for (Hit h : hits) if (h.in(mx, my)) return h.id;
		return -1;
	}

	private Hit hit(int id) {
		for (Hit h : hits) if (h.id == id) return h;
		return null;
	}

	/** Called from the Fabric click event (UiFx.onClick) - works reliably with touch launchers too. */
	public void handleClick(double mx, double my, int button) {
		int id = hitAt(mx, my);
		if (id != 91) focused = false;
		if (id < 0) return;
		long now = System.nanoTime();
		long guard = id >= 300 ? 70_000_000L : 220_000_000L; // keyboard keys may repeat quickly
		if (id == lastActionId && now - lastActionNs < guard) return; // de-duplicate event + polling paths
		if (id != lastActionId && now - lastActionNs < 40_000_000L) return;
		lastActionNs = now; lastActionId = id;
		pressId = id; pressNs = now;
		Config c = Config.INSTANCE;
		if (id != 85) dragScroll = false;
		if (id != 20 && dragging) { dragging = false; lastTickStep = -1; Config.save(); }
		if (id >= 60 && id < 64) { if (tab != id - 60) { tab = id - 60; tabStartNs = now; scrollPx = 0; scrollTarget = 0; } click(); }
		else if (id == 80 || id == 81) {
			scrollTarget += (id == 81 ? 1 : -1) * Math.max(30, sbH * 0.6f);
			click();
		}
		else if (id == 82) {
			Config.INSTANCE = new Config(); Config.save(); applyTheme(0); init(); click();
		}
		else if (id >= 100 && id < 100 + NS) {
			SoundStyle st = SoundStyle.values()[id - 100];
			c.style = st.name(); Config.save();
			pulse[id - 100] = 1f;
			Sounds.play(st, Sounds.Type.CLICK); // instant preview of the style you picked
			Hit hh = hit(id);
			if (hh != null && c.clickParticles) UiFx.burst(hh.x + hh.w / 2f, hh.y + hh.h / 2f, ACCENT[id - 100]);
		}
		else if (id == 20) { dragging = true; lastMoveNs = now; lastMx = mx; setVolumeFrom(mx); }
		else if (id == 21 || id == 22) {
			c.volume = Math.max(0f, Math.min(1f, Math.round((c.volume + (id == 22 ? 0.05f : -0.05f)) * 20) / 20f));
			Sounds.slide(c.volume); Config.save();
		}
		else if (id >= 30 && id < 34) Sounds.play(Sounds.Type.values()[id - 30]); // CLICK, OPEN, CLOSE, HOVER
		else if (id >= 70 && id < 76) {
			c.theme = id - 70; c.skin = false; applyTheme(c.theme); Config.save(); click();
			Hit th = hit(id);
			if (th != null && c.clickParticles) UiFx.burst(th.x + th.w / 2f, th.y + th.h / 2f, YEL & 0xFFFFFF);
		}
		else if (id >= 200 && id < 200 + toggles.size() * 2) {
			Toggle t = toggles.get((id - 200) / 2);
			t.set.accept(flat ? !t.get.get() : (id - 200) % 2 == 0); Config.save(); click();
		}
		else if (id == 85) { dragScroll = true; lastMoveYNs = now; lastMy = my; scrollFromY(my); }
		else if (id == 91) { focused = true; click(); }
		else if (id == 93) { // back to the normal look
			c.skin = false; Config.save(); applyTheme(c.theme); click();
			toast = "Default UI"; toastUntil = System.nanoTime() + 1_800_000_000L;
		}
		else if (id >= 300 && id < 333) { // on-screen keyboard (works without the phone keyboard)
			if (id == 331) { if (field.length() > 0) field.setLength(field.length() - 1); }
			else if (id == 332) submitCode();
			else { focused = true; addChar(id == 330 ? ' ' : 'a' + (id - 300), false); }
		}
		else if (id == 92) { click(); submitCode(); }
		else if (id == 90) { // tap the title 5 times = same as typing the secret code (for phones without a keyboard)
			titleTaps = (now - lastTitleTap < 1_500_000_000L) ? titleTaps + 1 : 1;
			lastTitleTap = now;
			if (titleTaps >= 5) { titleTaps = 0; toggleSkin(); } else click();
		}
		else if (id == 50 || id == 51) { click(); onClose(); }
	}

	private static boolean anyPressed() {
		var mh = Minecraft.getInstance().mouseHandler; // launchers may report a tap as the right/other button
		return mh.isLeftPressed() || mh.isRightPressed() || mh.isMiddlePressed();
	}

	private void scrollFromY(double my) {
		if (sbMax <= 0) return;
		float r = (float) Math.max(0, Math.min(1, (my - sbTop - sbThumb / 2.0) / Math.max(1, sbH - sbThumb)));
		scrollTarget = scrollPx = r * sbMax;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scrollTarget -= (float) scrollY * 26f;
		return true;
	}

	private void setVolumeFrom(double mx) {
		float v = (float) Math.max(0, Math.min(1, (mx - (sx + 6)) / (double) Math.max(1, sw - 12)));
		Config.INSTANCE.volume = Math.round(v * 20) / 20f; // 5% steps
		int step = Math.round(Config.INSTANCE.volume * 20);
		if (step != lastTickStep) { lastTickStep = step; Sounds.slide(Config.INSTANCE.volume); }
	}

	// secret code: type "action and stuff" in this menu to switch to the grey "Action" skin (type it again to go back)
	private static final String CODE = "actionandstuff";
	private String typed = "";
	private long toastUntil = 0;
	private String toast = "";

	private final StringBuilder field = new StringBuilder();
	private boolean focused = false;

	private void submitCode() {
		String n = field.toString().toLowerCase().replaceAll("[^a-z]", "");
		field.setLength(0);
		focused = false;
		if (n.equals(CODE)) toggleSkin();
		else { toast = "Wrong code"; toastUntil = System.nanoTime() + 1_800_000_000L; Sounds.play(Sounds.Type.CLOSE); }
	}

	private int lastKeyChar = -1;
	private long lastKeyNs = 0;

	/** Some launchers send letters as key presses, others as characters: accept both, never twice. */
	private void addChar(int cp, boolean fromKey) {
		long now = System.nanoTime();
		if (fromKey) { lastKeyChar = cp; lastKeyNs = now; }
		else if (cp == lastKeyChar && now - lastKeyNs < 80_000_000L) { lastKeyChar = -1; return; }
		if (cp >= 32 && cp != 127 && field.length() < 24) field.appendCodePoint(cp);
		if (field.toString().toLowerCase().replaceAll("[^a-z]", "").equals(CODE)) submitCode(); // no Enter needed
	}

	@Override
	public boolean charTyped(CharacterEvent e) {
		if (focused) { addChar(e.codepoint(), false); return true; }
		return super.charTyped(e);
	}

	private int titleTaps = 0;
	private long lastTitleTap = 0;

	private void toggleSkin() {
		Config c = Config.INSTANCE;
		c.skin = !c.skin; Config.save();
		applyTheme(c.skin ? 6 : c.theme);
		toast = c.skin ? "Action skin ON" : "Action skin OFF";
		toastUntil = System.nanoTime() + 2_200_000_000L;
		Sounds.play(Sounds.Type.OPEN);
		UiFx.burst(this.width / 2f, this.height / 2f, YEL & 0xFFFFFF);
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		int k = e.key();
		if (focused) { // typing into the secret-code box
			if (k == 259 && field.length() > 0) field.setLength(field.length() - 1); // backspace
			else if (k == 257 || k == 335) submitCode(); // enter
			else if (k == 256) focused = false; // escape: leave the box
			else if (k >= 65 && k <= 90) addChar(k + 32, true);
			else if (k == 32) addChar(32, true);
			return true;
		}
		if (k >= 65 && k <= 90) { // A-Z
			typed += (char) (k + 32);
			while (!typed.isEmpty() && !CODE.startsWith(typed)) typed = typed.substring(1);
			if (typed.equals(CODE)) {
				typed = "";
				toggleSkin();
				return true;
			}
			if (k == InputConstants.KEY_N && typed.length() <= 1) { typed = ""; onClose(); return true; }
			return true; // part of the code: do not let N close the menu
		}
		if (k == 32) return true; // space is part of the phrase
		if (k != 340 && k != 344 && k != 341 && k != 345) typed = "";
		return super.keyPressed(e);
	}

	@Override
	public void onClose() {
		Config.save();
		Minecraft.getInstance().gui.setScreen(parent);
	}

	// ---------------------------------------------------------------- drawing helpers
	private static void rr(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int c) {
		if (flat) r = 0;
		if (w <= 0 || h <= 0) return;
		r = Math.min(r, Math.min(w, h) / 2);
		g.fill(x, y + r, x + w, y + h - r, c);
		for (int i = 0; i < r; i++) {
			double d = r - i - 0.5;
			int inset = r - (int) Math.round(Math.sqrt(r * r - d * d));
			g.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
			g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, c);
		}
	}

	private static int lerp(int a, int b, float t) {
		t = Math.max(0, Math.min(1, t));
		int al = (int) (((a >>> 24) & 255) * (1 - t) + ((b >>> 24) & 255) * t);
		int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
		int gg = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
		int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
		return (al << 24) | (r << 16) | (gg << 8) | bl;
	}

	private static void rrGrad(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int top, int bot) {
		if (flat) r = 0;
		for (int j = 0; j < h; j++) {
			int inset = 0;
			if (j < r) { double d = r - j - 0.5; inset = r - (int) Math.round(Math.sqrt(r * r - d * d)); }
			else if (j >= h - r) { double d = j - (h - r) + 0.5; inset = r - (int) Math.round(Math.sqrt(r * r - d * d)); }
			g.fill(x + inset, y + j, x + w - inset, y + j + 1, lerp(top, bot, j / (float) Math.max(1, h - 1)));
		}
	}

	private void textC(GuiGraphicsExtractor g, String s, int cx, int y, int color) {
		Font f = getFont();
		tx(g, f, s, cx - tw(f, s) / 2, y, color);
	}

	/** Text helper: in the Action skin the font is drawn without the dark drop shadow (it smeared dark text). */
	private static void tx(GuiGraphicsExtractor g, Font f, String s, int x, int y, int c) {
		if (flat) PixelFont.draw(g, s, x, y, c);
		else g.text(f, s, x, y, c);
	}

	private static Component bold(String s) {
		Component c = Component.literal(s).withStyle(ChatFormatting.BOLD);
		return flat ? Component.literal(s).withStyle(ChatFormatting.BOLD).withStyle(st -> st.withShadowColor(0)) : c;
	}

	/** Bevelled button in the Action skin, rounded rect otherwise. */
	private static void btn(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int c) {
		if (!flat) { rr(g, x, y, w, h, r, c); return; }
		g.fill(x, y, x + w, y + h, c);
		g.fill(x, y, x + w, y + 1, lerp(c, 0xFFFFFFFF, 0.6f));
		g.fill(x, y, x + 1, y + h, lerp(c, 0xFFFFFFFF, 0.6f));
		g.fill(x, y + h - 1, x + w, y + h, lerp(c, 0xFF000000, 0.35f));
		g.fill(x + w - 1, y, x + w, y + h, lerp(c, 0xFF000000, 0.35f));
	}

	private static void icon(GuiGraphicsExtractor g, SoundStyle st, int x, int y, int accent) {
		if (flat) Icons.styleTile(g, st, x, y, 12);
		else Icons.style(g, st, x, y, 1, 255);
	}

	private static int contrast(int bg) {
		int lum = (((bg >> 16) & 255) * 3 + ((bg >> 8) & 255) * 6 + (bg & 255)) / 10;
		return lum > 150 ? 0xFF161616 : 0xFFFFFFFF;
	}

	private static float ease(float t) { t = Math.max(0, Math.min(1, t)); return 1 - (1 - t) * (1 - t) * (1 - t); }

	private float hv(int id) { return hov.getOrDefault(id, 0f); }

	private static int tw(Font f, String s) { return flat ? PixelFont.width(s) : f.width(s); }

	private static String fit(Font f, String s, int w) {
		while (s.length() > 1 && tw(f, s) > w) s = s.substring(0, s.length() - 1);
		return s;
	}

	private Hit add(int x, int y, int w, int h, int id) {
		Hit h2 = new Hit(x, y, w, h, id);
		hits.add(h2);
		return h2;
	}

	// ---------------------------------------------------------------- rendering
	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
		if (flat) return; // Action skin: no dim, no sparkles - the game stays visible
		g.fillGradient(0, 0, this.width, this.height, 0xD0140F2E, 0xEE08061A);
		float t = (System.nanoTime() - startNs) / 1e9f;
		for (int i = 0; i < 22; i++) { // drifting sparkles
			int bx = (i * 137 + 29) % Math.max(1, this.width);
			float speed = 6 + (i % 5) * 3;
			int y = this.height - (int) ((t * speed + i * 53) % (this.height + 20));
			int x = bx + (int) (Math.sin(t * 0.8 + i) * 4);
			int a = (int) (50 + 90 * Math.abs(Math.sin(t * 1.7 + i * 1.3)));
			int col = (a << 24) | ((i % 3 == 0 ? MINT : i % 3 == 1 ? PINK : YEL) & 0xFFFFFF);
			g.fill(x, y - 1, x + 1, y + 2, col);
			g.fill(x - 1, y, x + 2, y + 1, col);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.05f, (now - lastNs) / 1e9f);
		lastNs = now;
		float t = (now - startNs) / 1e9f;
		Config c = Config.INSTANCE;
		Font font = getFont();

		// slider drag (touch friendly: keeps following the pointer while it moves)
		if (dragging) {
			if (mx != lastMx) { lastMx = mx; lastMoveNs = now; }
			boolean nearY = my >= sy - 30 && my <= sy + 36;
			if (anyPressed() || (nearY && now - lastMoveNs < 600_000_000L)) setVolumeFrom(mx);
			else { dragging = false; lastTickStep = -1; Config.save(); }
		}
		// fallback click path: detect the press ourselves (de-duplicated in handleClick)
		boolean down = anyPressed();
		if (down && !wasDown) handleClick(mx, my, 0);
		wasDown = down;

		// hover state + smoothing (uses last frame's hit boxes)
		int h = hitAt(mx, my);
		if (h != hoverId) {
			hoverId = h;
			if (h >= 0 && c.hoverSound) Sounds.play(Sounds.Type.HOVER);
		}
		float k = Math.min(1f, dt * 14f);
		for (Hit ht : hits) hov.put(ht.id, hv(ht.id) + ((hoverId == ht.id ? 1f : 0f) - hv(ht.id)) * k);
		for (int i = 0; i < pulse.length; i++) pulse[i] = Math.max(0, pulse[i] - dt * 3.5f);
		for (int i = 0; i < toggles.size(); i++) swT[i] += ((toggles.get(i).get().get() ? 1f : 0f) - swT[i]) * k;
		hits.clear();

		// ---- geometry
		pw = Math.min(this.width - 8, flat ? 560 : 400);
		ph = Math.min(this.height - 8, flat ? 250 : 240);
		px = (this.width - pw) / 2;
		int oy = c.animations ? (int) ((1 - ease(t / 0.3f)) * 16) : 0;
		py = (this.height - ph) / 2 + oy;

		// ---- shell + title bar
		rr(g, px - 1, py + 2, pw + 2, ph + 2, 6, 0x55000000);
		rrGrad(g, px - 1, py - 1, pw + 2, ph + 2, 6, flat ? 0xFF111111 : MINT, flat ? 0xFF111111 : PINK);
		rr(g, px, py, pw, ph, 5, CARD);
		if (flat) g.fill(px + 1, py + 1, px + pw - 1, py + 17, HDR); else g.fillGradient(px + 1, py + 3, px + pw - 1, py + 16, HDR, CARD);
		g.fill(px + 4, py + 1, px + pw - 4, py + 3, HDR);
		String title = "SOFT CHIME UI";
		int tw0 = flat ? PixelFont.width(title) : font.width(bold(title));
		if (!flat) {
			g.text(font, bold(title), px + (pw - tw0) / 2 + 1, py + 5, TSH);
			g.text(font, bold(title), px + (pw - tw0) / 2, py + 4, TC);
		} else PixelFont.draw(g, title, px + (pw - tw0) / 2, py + 4, TC);
		Hit cl = add(px + pw - 16, py + 2, 13, 13, 51);
		add(px + 40, py + 1, pw - 80, 16, 90);
		rr(g, cl.x, cl.y, 13, 13, 4, lerp(CARD3, PINK, hv(51)));
		for (int i = 0; i < 5; i++) {
			g.fill(cl.x + 4 + i, cl.y + 4 + i, cl.x + 5 + i, cl.y + 5 + i, TXT);
			g.fill(cl.x + 8 - i, cl.y + 4 + i, cl.x + 9 - i, cl.y + 5 + i, TXT);
		}

		int bodyY = py + 18, bodyH = ph - 20;
		int sw0 = flat ? Math.max(62, Math.min(92, pw * 22 / 100)) : Math.max(74, Math.min(104, pw * 27 / 100));
		int sbx = px + 4;
		rr(g, sbx, bodyY, sw0, bodyH, 4, DEEP);

		if (System.nanoTime() < toastUntil) {
			int tw5 = tw(font, toast);
			rr(g, (this.width - tw5) / 2 - 6, py + ph + 4, tw5 + 12, 14, 3, HDR);
			tx(g, font, toast, (this.width - tw5) / 2, py + ph + 7, TC);
		}

		// ---- sidebar
		int y = bodyY + 4;
		if (flat) { // Action skin: the banner image supplied by the user
			int bw = sw0 - 8, bh = bw * 536 / 960;
			g.blit(RenderPipelines.GUI_TEXTURED, BANNER, sbx + 4, y, 0, 0, bw, bh, 960, 536, 960, 536);
			if (c.animations) { // shine sweeps across the banner every few seconds
				float sp = (t % 4f) / 0.8f;
				if (sp < 1f) {
					int bxs = sbx + 4 + (int) (sp * (bw + 24)) - 12;
					for (int sk = 0; sk < 5; sk++) {
						int x0 = Math.max(sbx + 4, bxs + sk * 2), x1 = Math.min(sbx + 4 + bw, bxs + sk * 2 + 2);
						if (x1 > x0) g.fill(x0, y, x1, y + bh, ((int) (60 * (1 - Math.abs(sk - 2) / 3f)) << 24) | 0xFFFFFF);
					}
				}
			}
			y += bh + 4;
		} else if (bodyH >= 170) {
			rrGrad(g, sbx + 4, y, sw0 - 8, 36, 4, lerp(MINT, CARD, 0.6f), lerp(PINK, CARD, 0.6f));
			Icons.logo(g, sbx + (sw0 - 32) / 2, y + 2, 1);
			y += 39;
			textC(g, "v" + VERSION, sbx + sw0 / 2, y, MUTED);
			y += 11;
		}
		Hit done = add(sbx + 4, y, sw0 - 8, 14, 50);
		rr(g, done.x, done.y + 1, done.w, done.h, 4, 0xFFB5902A);
		btn(g, done.x, done.y, done.w, done.h, 4, lerp(YEL, 0xFFFFFFFF, hv(50) * 0.4f));
		textC(g, "DONE", done.x + done.w / 2, done.y + 3, DARK);
		if (c.animations) g.outline(done.x - 1, done.y - 1, done.w + 2, done.h + 2, (((int) (40 + 70 * (0.5 + 0.5 * Math.sin(t * 3)))) << 24) | (YEL & 0xFFFFFF));
		y += 19;
		for (int i = 0; i < 4; i++) {
			int rowH = flat ? 15 : 18;
			Hit th = add(sbx + 2, y, sw0 - 4, rowH, 60 + i);
			boolean sel = tab == i;
			if (sel) {
				if (selY < 0) selY = th.y;
				selY += (th.y - selY) * Math.min(1f, dt * 16f); // glides to the chosen category
				rr(g, th.x, Math.round(selY), th.w, th.h, 3, SIDESEL);
				g.fill(th.x, Math.round(selY) + 3, th.x + 2, Math.round(selY) + th.h - 3, YEL);
			}
			else if (hv(60 + i) > 0.02f) rr(g, th.x, th.y, th.w, th.h, 3, lerp(DEEP, SIDESEL, hv(60 + i)));
			int slide = flat && c.animations ? Math.round(2 * hv(60 + i)) : 0;
			icon(g, TAB_ICON[i], th.x + (flat ? 4 : 5) + slide, th.y + 1, ACCENT[TAB_ICON[i].ordinal()]);
			tx(g, font, TAB_NAMES[i], th.x + (flat ? 20 : 25) + slide, th.y + (flat ? 4 : 5), sel ? WHITE : lerp(MUTED, WHITE, hv(60 + i)));
			y += flat ? 16 : 19;
		}

		if (flat) {
			Hit dflt = add(sbx + 4, y + 4, sw0 - 8, 14, 93);
			btn(g, dflt.x, dflt.y, dflt.w, dflt.h, 4, lerp(CARD2, CARD3, hv(93)));
			textC(g, "Default UI", dflt.x + dflt.w / 2, dflt.y + 3, TXT);
		}

		// ---- content header
		int cx = px + sw0 + 10, cw = px + pw - 5 - cx;
		float tin = ease((now - tabStartNs) / 1e9f / 0.22f);
		int hdrDx = c.animations ? (int) ((1 - tin) * 10) : 0;
		String ct = TAB_NAMES[tab].toUpperCase();
		if (!flat) {
			g.text(font, bold(ct), cx + hdrDx + 1, bodyY + 4, TSH);
			g.text(font, bold(ct), cx + hdrDx, bodyY + 3, HC);
		} else PixelFont.draw(g, ct, cx + hdrDx, bodyY + 3, HC);
		tx(g, font, fit(font, TAB_SUB[tab], cw - 80), cx + hdrDx, bodyY + 14, MUTED);
		// reset + scroll arrows
		Hit rs = add(cx + cw - 32, bodyY + 2, 32, 12, 82);
		btn(g, rs.x, rs.y, rs.w, rs.h, 3, lerp(CARD2, flat ? YEL : PINK, hv(82)));
		textC(g, "Reset", rs.x + rs.w / 2, rs.y + 2, TXT);

		// ---- item list (smooth pixel scrolling, clipped to the panel)
		List<Item> items = items();
		int viewTop = bodyY + 27, viewBottom = bodyY + bodyH - 3, viewH = viewBottom - viewTop;
		int total = 0;
		for (Item it0 : items) total += it0.h + 2;
		int maxScroll = Math.max(0, total - viewH);
		scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));
		if (dragScroll) {
			if (my != lastMy) { lastMy = my; lastMoveYNs = now; }
			if (anyPressed() || now - lastMoveYNs < 600_000_000L) scrollFromY(my); else dragScroll = false;
		}
		scrollPx += (scrollTarget - scrollPx) * Math.min(1f, dt * 14f);
		if (Math.abs(scrollTarget - scrollPx) < 0.3f) scrollPx = scrollTarget;
		sbTop = viewTop; sbH = viewH; sbMax = maxScroll;
		int listW = cw - (maxScroll > 0 ? 9 : 0);
		g.enableScissor(cx - 2, viewTop, cx + cw + 2, viewBottom);
		int yy = viewTop - Math.round(scrollPx), idx = 0;
		for (Item it : items) {
			if (yy + it.h >= viewTop && yy <= viewBottom) {
				float ent = c.animations ? ease((now - tabStartNs) / 1e9f / 0.25f - idx * 0.1f) : 1f;
				int iy = yy + (int) ((1 - ent) * 8);
				int before = hits.size();
				drawItem(g, font, it, cx, iy, listW, t);
				for (int q = hits.size() - 1; q >= before; q--) { // keep only the visible part clickable
					Hit h0 = hits.get(q);
					int top = Math.max(h0.y, viewTop), bot = Math.min(h0.y + h0.h, viewBottom);
					if (bot <= top) hits.remove(q); else hits.set(q, new Hit(h0.x, top, h0.w, bot - top, h0.id));
				}
			}
			yy += it.h + 2;
			idx++;
		}
		g.disableScissor();
		if (maxScroll > 0) { // scrollbar
			sbThumb = Math.max(16, viewH * viewH / total);
			int ty = viewTop + Math.round((viewH - sbThumb) * (scrollPx / maxScroll));
			int bx = cx + cw - 5;
			g.fill(bx, viewTop, bx + 5, viewBottom, flat ? 0xFF262626 : DEEP);
			Hit sbh = add(cx + cw - 12, viewTop, 12, viewH, 85);
			int tc = flat ? lerp(0xFFA8A8A8, 0xFFFFFFFF, hv(85)) : lerp(MUTED, WHITE, hv(85));
			g.fill(bx, ty, bx + 5, ty + sbThumb, tc);
			g.fill(bx, ty, bx + 5, ty + 1, lerp(tc, 0xFFFFFFFF, 0.5f));
		}
		if (c.animations) {
			for (Hit ht : hits) {
				float hvv = hv(ht.id);
				if ((ht.id >= 60 && ht.id < 64) || (ht.id >= 200 && ht.id < 300) || ht.id == 85) continue;
				if (hvv > 0.03f && ht.h >= 12 && ht.h <= 24 && ht.w >= 20) { // underline grows from the centre
					int lw = (int) (ht.w * hvv);
					g.fill(ht.x + (ht.w - lw) / 2, ht.y + ht.h - 1, ht.x + (ht.w + lw) / 2, ht.y + ht.h, 0xFF000000 | (YEL & 0xFFFFFF));
				}
			}
			if (pressId >= 0) { // quick flash when something is tapped
				float pp = (now - pressNs) / 220_000_000f;
				Hit ph2 = hit(pressId);
				if (ph2 != null && pp >= 0 && pp < 1) g.fill(ph2.x, ph2.y, ph2.x + ph2.w, ph2.y + ph2.h, ((int) (110 * (1 - pp)) << 24) | 0xFFFFFF);
			}
		}
		boolean canUp = scrollTarget > 0.5f, canDown = scrollTarget < maxScroll - 0.5f;
		for (int i = 0; i < 2; i++) {
			boolean up = i == 0, en = up ? canUp : canDown;
			int ax = cx + cw - 32 - 4 - (up ? 28 : 14);
			Hit ah = add(ax, bodyY + 2, 12, 12, up ? 80 : 81);
			btn(g, ah.x, ah.y, 12, 12, 3, en ? lerp(CARD2, CARD3, hv(ah.id)) : DEEP);
			int mxA = ah.x + 6, myA = ah.y + 6;
			int col = en ? TXT : OFF;
			for (int r = 0; r < 3; r++) {
				int dy = up ? -1 + r : 1 - r;
				g.fill(mxA - r, myA + dy, mxA + r + 1, myA + dy + 1, col);
			}
		}
	}

	private void drawItem(GuiGraphicsExtractor g, Font font, Item it, int x, int y, int w, float t) {
		Config c = Config.INSTANCE;
		switch (it.kind) {
			case 0 -> {
				if (flat) {
					icon(g, SoundStyle.values()[it.a], x, y + 2, ACCENT[it.a]);
					tx(g, font, it.s1, x + 17, y + 2, WHITE);
					tx(g, font, fit(font, it.s2, w - 19), x + 17, y + 10, MUTED);
					break;
				}
				icon(g, SoundStyle.values()[it.a], x, y + 3, ACCENT[it.a]);
				tx(g, font, it.s1, x + 20, y + 3, WHITE);
				tx(g, font, fit(font, it.s2, w - 22), x + 20, y + 13, MUTED);
			}
			case 1 -> {
				int cw4 = (w - 9) / 4;
				SoundStyle cur = c.style();
				for (int k = 0; k < 4; k++) {
					int i = it.a * 4 + k;
					if (i >= NS) break;
					int cx = x + k * (cw4 + 3);
					Hit hh = add(cx, y, cw4, it.h - 2, 100 + i);
					boolean sel = cur.ordinal() == i;
					float hvv = hv(100 + i);
					int cy = y - (int) (hvv * 1);
					rr(g, cx, cy + 1, cw4, hh.h, 4, 0x55000000);
					btn(g, cx, cy, cw4, hh.h, 4, lerp(CARD2, CARD3, hvv));
					if (sel) {
						rr(g, cx - 1, cy - 1, cw4 + 2, hh.h + 2, 5, YEL);
						btn(g, cx, cy, cw4, hh.h, 4, lerp(SEL1, SEL2, hvv));
						if (c.animations) {
							int ga = (int) (70 + 90 * (0.5 + 0.5 * Math.sin(t * 4)));
							g.outline(cx - 2, cy - 2, cw4 + 4, hh.h + 4, (ga << 24) | (ACCENT[i] & 0xFFFFFF));
						}
					}
					g.fill(cx + 1, cy + 4, cx + 2, cy + hh.h - 4, ACCENT[i]);
					int bounce = (int) (Math.sin(pulse[i] * Math.PI) * 3);
					int isz = flat ? 12 : 16, tOff = flat ? 18 : 21;
					icon(g, SoundStyle.values()[i], cx + 3, cy + (hh.h - isz) / 2 - bounce, ACCENT[i]);
					tx(g, font, fit(font, NAMES[i], cw4 - tOff - 3), cx + tOff, cy + (hh.h - 8) / 2, TXT);
					if (sel) for (int b = 0; b < 3; b++) {
						int bh = 1 + (int) (Math.abs(Math.sin(t * 6 + b * 1.7)) * 4);
						g.fill(cx + cw4 - 7 + b * 2, cy + hh.h - 3 - bh, cx + cw4 - 6 + b * 2, cy + hh.h - 3, ACCENT[i]);
					}
				}
			}
			case 2 -> {
				int pctW = 28;
				Hit mn = add(x, y + 3, 16, 14, 21);
				Hit pl = add(x + w - pctW - 18, y + 3, 16, 14, 22);
				for (Hit bh : new Hit[] {mn, pl}) {
					btn(g, bh.x, bh.y, bh.w, bh.h, 4, lerp(CARD2, CARD3, hv(bh.id)));
					int bx = bh.x + bh.w / 2, by = bh.y + bh.h / 2;
					g.fill(bx - 3, by, bx + 4, by + 1, TXT);
					if (bh.id == 22) g.fill(bx, by - 3, bx + 1, by + 4, TXT);
				}
				sx = x + 20; sw = pl.x - 4 - sx; sy = y + 10;
				add(sx - 4, y, sw + 8, it.h, 20);
				rr(g, sx, sy, sw, 6, 3, DEEP);
				if (volShown < 0) volShown = c.volume;
				volShown += (c.volume - volShown) * Math.min(1f, 0.35f);
				int kx = sx + 6 + Math.round(volShown * (sw - 12));
				for (int i = 0; i < kx - sx; i++) g.fill(sx + i, sy + 1, sx + i + 1, sy + 5, lerp(MINT, YEL, i / (float) Math.max(1, sw)));
				rr(g, kx - 6, sy - 3, 12, 12, 6, PINK);
				rr(g, kx - 5, sy - 2, 10, 10, 5, WHITE);
				String pct = Math.round(c.volume * 100) + "%";
				tx(g, font, pct, x + w - tw(font, pct), y + 7, YEL);
			}
			case 3 -> {
				String[] names = {"Click", "Open", "Close", "Hover"};
				int chw = (w - 9) / 4;
				for (int i = 0; i < 4; i++) {
					Hit ch = add(x + i * (chw + 3), y, chw, 16, 30 + i);
					btn(g, ch.x, ch.y, ch.w, ch.h, 4, lerp(CARD2, CARD3, hv(30 + i)));
					int tw3 = tw(font, names[i]);
					int startX = ch.x + (ch.w - (tw3 + 10)) / 2;
					int[] tri = {1, 2, 3, 2, 1};
					for (int r = 0; r < 5; r++) g.fill(startX, ch.y + 5 + r, startX + tri[r], ch.y + 6 + r, MINT);
					tx(g, font, names[i], startX + 10, ch.y + 4, TXT);
				}
			}
			case 4 -> {
				Toggle tg = toggles.get(it.a);
				float sw1 = swT[it.a];
				if (!flat) { // default UI: classic On/Off buttons
					float s = sw1;
					rr(g, x, y, w, it.h - 2, 4, CARD2);
					rr(g, x + 5, y + 8, 8, 8, 2, lerp(OFF, MINT, s));
					int bw = 26, onX = x + w - 2 * bw - 6, offX = onX + bw + 2;
					tx(g, font, fit(font, tg.label, onX - x - 22), x + 18, y + 4, TXT);
					tx(g, font, fit(font, tg.desc, onX - x - 22), x + 18, y + 14, MUTED2);
					Hit on = add(onX, y + 5, bw, 14, 200 + it.a * 2);
					Hit off = add(offX, y + 5, bw, 14, 201 + it.a * 2);
					rr(g, on.x, on.y, bw, 14, 3, lerp(lerp(CARD3, CARD2, 0.3f), YEL, s));
					rr(g, off.x, off.y, bw, 14, 3, lerp(lerp(CARD3, CARD2, 0.3f), YEL, 1 - s));
					textC(g, "On", on.x + bw / 2, on.y + 3, s > 0.5f ? DARK : MUTED2);
					textC(g, "Off", off.x + bw / 2, off.y + 3, s <= 0.5f ? DARK : MUTED2);
					break;
				}
				Hit row = add(x, y, w, it.h - 2, 200 + it.a * 2);
				float hvr = hv(row.id);
				if (flat) {
					g.fill(x, y + it.h - 1, x + w, y + it.h, 0xFF1F1F1F);
					if (hvr > 0.02f) g.fill(x, y, x + w, y + it.h - 1, ((int) (24 * hvr) << 24) | 0xFFFFFF);
					if (hvr > 0.02f) g.fill(x, y + 2, x + Math.round(3 * hvr), y + it.h - 3, YEL);
				} else rr(g, x, y, w, it.h - 2, 4, lerp(CARD2, CARD3, hvr));
				int ai = (it.a * 3 + 1) % NS;
				icon(g, SoundStyle.values()[ai], x + 3, y + (it.h - 2 - 12) / 2, ACCENT[ai]);
				int swW = 34, swH = 12, sx0 = x + w - swW - 6, sy0 = y + (it.h - 2 - swH) / 2;
				tx(g, font, fit(font, tg.label, sx0 - x - 22), x + 19, y + 2, WHITE);
				tx(g, font, fit(font, tg.desc, sx0 - x - 22), x + 19, y + 10, MUTED);
				{
				// Action skin: yellow "I" side when on, knob slides smoothly
				g.fill(sx0 - 1, sy0 - 1, sx0 + swW + 1, sy0 + swH + 1, 0xFF111111);
				g.fill(sx0, sy0, sx0 + swW, sy0 + swH, 0xFF252525);
				int half = swW / 2, kx = sx0 + Math.round(sw1 * half);
				if (sw1 > 0.02f) {
					g.fill(sx0, sy0, kx + 1, sy0 + swH, 0xFF000000 | (YEL & 0xFFFFFF));
					g.fill(sx0, sy0 + swH - 1, kx + 1, sy0 + swH, lerp(YEL, 0xFF000000, 0.3f));
				}
				if (sw1 > 0.5f) g.fill(sx0 + half / 2 - 1, sy0 + 3, sx0 + half / 2 + 1, sy0 + swH - 3, 0xFFFFE9A8);
				else { int ox = sx0 + half + half / 2 - 2; g.outline(ox, sy0 + 3, 4, swH - 6, 0xFF9A9A9A); }
				btn(g, kx, sy0, half, swH, 2, lerp(0xFFCFCFCF, 0xFFFFFFFF, hvr * 0.6f));
				}
			}
			case 5 -> {
				int tw3 = (w - 6) / 3;
				for (int k = 0; k < 3; k++) {
					int i = it.a * 3 + k;
					if (i >= 6) break;
					int[] th = THEMES[i];
					int cx = x + k * (tw3 + 3);
					Hit hh = add(cx, y, tw3, it.h - 2, 70 + i);
					boolean sel = !c.skin && c.theme == i;
					if (sel) {
						rr(g, cx - 1, y - 1, tw3 + 2, hh.h + 2, 5, th[6]);
						if (c.animations) g.outline(cx - 2, y - 2, tw3 + 4, hh.h + 4, (((int) (70 + 90 * (0.5 + 0.5 * Math.sin(t * 4)))) << 24) | (th[6] & 0xFFFFFF));
					}
					rr(g, cx, y, tw3, hh.h, 4, lerp(th[1], th[2], hv(70 + i)));
					for (int q = 0; q < 3; q++) rr(g, cx + 4 + q * 7, y + 4, 6, hh.h - 8, 2, th[5 + q]);
					tx(g, font, fit(font, THEME_NAMES[i], tw3 - 28), cx + 26, y + (hh.h - 8) / 2, contrast(THEMES[i][1]));
				}
			}
			case 7 -> {
				int enW = 40;
				Hit fb = add(x, y, w - enW - 4, 20, 91);
				rr(g, fb.x, fb.y, fb.w, fb.h, 4, DEEP);
				if (focused) g.outline(fb.x - 1, fb.y - 1, fb.w + 2, fb.h + 2, YEL);
				String shown = field.toString();
				int maxW = fb.w - 10;
				while (shown.length() > 1 && tw(font, shown) > maxW) shown = shown.substring(1);
				if (shown.isEmpty() && !focused) tx(g, font, "Tap here to type...", fb.x + 5, fb.y + 6, MUTED);
				else {
					tx(g, font, shown, fb.x + 5, fb.y + 6, WHITE);
					if (focused && ((int) (t * 2) % 2 == 0)) g.fill(fb.x + 5 + tw(font, shown), fb.y + 4, fb.x + 6 + tw(font, shown), fb.y + 15, YEL);
				}
				Hit en = add(x + w - enW, y, enW, 20, 92);
				rr(g, en.x, en.y + 1, en.w, en.h, 4, 0xFFB5902A);
				rr(g, en.x, en.y, en.w, en.h, 4, lerp(YEL, 0xFFFFFFFF, hv(92) * 0.4f));
				textC(g, "Enter", en.x + en.w / 2, en.y + 6, DARK);
			}
			case 8 -> {
				String[] kr = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
				int kw = (w - 9 * 2) / 10;
				for (int r = 0; r < 3; r++) {
					int rw = kr[r].length() * (kw + 2) - 2 + (r == 2 ? kw * 2 + 4 : 0);
					int rx = x + (w - rw) / 2;
					for (int i = 0; i < kr[r].length(); i++) {
						int kx2 = rx + i * (kw + 2);
						Hit kh = add(kx2, y + r * 16, kw, 14, 300 + (kr[r].charAt(i) - 'a'));
						btn(g, kh.x, kh.y, kh.w, kh.h, 3, lerp(CARD2, CARD3, hv(kh.id)));
						textC(g, String.valueOf(kr[r].charAt(i)).toUpperCase(), kh.x + kh.w / 2, kh.y + 3, TXT);
					}
					if (r == 2) {
						int bx2 = rx + kr[r].length() * (kw + 2);
						Hit bs = add(bx2, y + r * 16, kw * 2 + 2, 14, 331);
						btn(g, bs.x, bs.y, bs.w, bs.h, 3, lerp(CARD2, PINK, hv(331)));
						textC(g, "<-", bs.x + bs.w / 2, bs.y + 3, TXT);
					}
				}
				Hit sp = add(x + (w - 154) / 2, y + 48, 110, 14, 330);
				btn(g, sp.x, sp.y, sp.w, sp.h, 3, lerp(CARD2, CARD3, hv(330)));
				textC(g, "space", sp.x + sp.w / 2, sp.y + 3, TXT);
				Hit en2 = add(sp.x + sp.w + 4, y + 48, 40, 14, 332);
				btn(g, en2.x, en2.y, en2.w, en2.h, 3, lerp(YEL, 0xFFFFFFFF, hv(332) * 0.4f));
				textC(g, "Enter", en2.x + en2.w / 2, en2.y + 3, DARK);
			}
			default -> tx(g, font, it.s1, x, y + 2, it.a == 0 ? YEL : MUTED);
		}
	}
}
