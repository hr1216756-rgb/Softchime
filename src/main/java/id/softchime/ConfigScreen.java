package id.softchime;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Custom-drawn settings panel (Sound / Effects tabs). Open with N or from the Options screen. */
public class ConfigScreen extends Screen {
	// palette taken from the mod icon
	private static final int CARD = 0xFF221C45, CARD2 = 0xFF2D2659, CARD3 = 0xFF3A3170, DEEP = 0xFF1A1538;
	private static final int MINT = 0xFF6BF5D0, YEL = 0xFFFFD866, PINK = 0xFFFF5FA2, WHITE = 0xFFFFFFFF;
	private static final int MUTED = 0xFFA79FD6, OFF = 0xFF4A4378;

	private static final String[] NAMES = {"Chime", "Bubble", "Marimba", "Crystal", "Retro", "Tap", "Music Box"};
	private static final String[] TAGS = {"gentle bell", "playful pop", "warm wood", "sparkly glass", "8-bit beep", "subtle tap", "soft whisper"};
	private static final int[] ACCENT = {YEL, 0xFF6BD6FF, 0xFFE8903A, MINT, 0xFF7DFF8A, PINK, 0xFFB48CFF};
	private static final int NS = SoundStyle.values().length;

	private record Hit(int x, int y, int w, int h, int id) {
		boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
	}
	private record Toggle(String label, Supplier<Boolean> get, Consumer<Boolean> set) {}

	private final Screen parent;
	private int tab = 0;
	private int px, py, pw, ph, headerH;
	private boolean hint;
	private final List<Hit> hits = new ArrayList<>();
	private final List<Toggle> toggles = new ArrayList<>();
	private int sx, sy, sw; // volume track
	private boolean dragging;
	private int lastTickStep = -1;

	private float tabPos = 0;
	private final float[] hoverT = new float[8];
	private final float[] pulse = new float[8];
	private final float[] swT = new float[12];
	private int hoverId = -1;
	private long lastActionNs = 0;
	private boolean wasDown = false;
	private String dbg = "";
	private static final String VERSION = FabricLoader.getInstance().getModContainer("softchime")
		.map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?");
	private final long startNs = System.nanoTime();
	private long lastNs = startNs;

	public ConfigScreen(Screen parent) {
		super(Component.literal("Soft Chime UI"));
		this.parent = parent;
	}

	// ---------------------------------------------------------------- layout
	@Override
	protected void init() {
		Config c = Config.INSTANCE;
		toggles.clear();
		toggles.add(new Toggle("Click sound", () -> c.clickSound, v -> c.clickSound = v));
		toggles.add(new Toggle("Open / close sound", () -> c.openCloseSound, v -> c.openCloseSound = v));
		toggles.add(new Toggle("Hover sound", () -> c.hoverSound, v -> c.hoverSound = v));
		toggles.add(new Toggle("Cursor ring", () -> c.cursorRing, v -> c.cursorRing = v));
		toggles.add(new Toggle("Click effect", () -> c.clickParticles, v -> c.clickParticles = v));
		toggles.add(new Toggle("Animations", () -> c.animations, v -> c.animations = v));
		if (UiFx.REANIMATED) toggles.add(new Toggle("ReAnimated compat", () -> c.reanimatedCompat, v -> c.reanimatedCompat = v));
		toggles.add(new Toggle("Options button", () -> c.optionsButton, v -> c.optionsButton = v));
		for (int i = 0; i < toggles.size(); i++) swT[i] = toggles.get(i).get().get() ? 1f : 0f;
		layout();
	}

	private void layout() {
		hits.clear();
		boolean compact = this.height < 275;
		headerH = compact ? 40 : 46;
		int cardH = compact ? 24 : 28;
		hint = !compact;
		int content = tab == 0
			? 11 + 2 * cardH + 4 + 8 + 26 + 6 + 18
			: ((toggles.size() + 1) / 2) * 26;
		ph = headerH + 28 + content + 8 + 22 + (hint ? 14 : 8) + 4;
		if (ph > this.height - 4) { cardH = 22; hint = false; ph = headerH + 28 + (tab == 0 ? 11 + 2 * cardH + 4 + 8 + 26 + 6 + 18 : ((toggles.size() + 1) / 2) * 26) + 8 + 22 + 8 + 4; }
		pw = Math.min(this.width - 10, 340);
		px = (this.width - pw) / 2;
		py = Math.max(2, (this.height - ph) / 2);
		int ix = px + 12, iw = pw - 24;

		hits.add(new Hit(px + pw - 22, py + 6, 16, 16, 51)); // close
		int tabY = py + headerH;
		int tw = (iw - 4) / 2;
		hits.add(new Hit(ix, tabY, tw + 2, 20, 0));
		hits.add(new Hit(ix + tw + 2, tabY, iw - tw - 2, 20, 1));

		int y = tabY + 28;
		if (tab == 0) {
			y += 11;
			int cw = (iw - 12) / 4;
			for (int i = 0; i < NS; i++)
				hits.add(new Hit(ix + (i % 4) * (cw + 4), y + (i / 4) * (cardH + 4), cw, cardH, 10 + i));
			y += 2 * cardH + 4 + 8;
			sx = ix; sy = y + 12; sw = iw;
			hits.add(new Hit(ix, y, iw, 26, 20));
			y += 26 + 6;
			int chw = (iw - 12) / 4;
			for (int i = 0; i < 4; i++) hits.add(new Hit(ix + i * (chw + 4), y, chw, 18, 30 + i));
		} else {
			int cw = (iw - 4) / 2;
			for (int i = 0; i < toggles.size(); i++)
				hits.add(new Hit(ix + (i % 2) * (cw + 4), y + (i / 2) * 26, cw, 22, 40 + i));
		}
		hits.add(new Hit(ix, py + ph - (hint ? 14 : 8) - 4 - 22, iw, 22, 50)); // done
	}

	private Hit hit(int id) {
		for (Hit h : hits) if (h.id == id) return h;
		return null;
	}

	private int hitAt(double mx, double my) {
		for (Hit h : hits) if (h.in(mx, my)) return h.id;
		return -1;
	}

	// ---------------------------------------------------------------- input
	private static void click() { Sounds.play(Sounds.Type.CLICK); }

	/** Called from the Fabric click event (UiFx.onClick) - works reliably with touch launchers too. */
	public void handleClick(double mx, double my, int button) {
		int id = hitAt(mx, my);
		dbg = "click " + (int) mx + "," + (int) my + " btn" + button + " -> " + id;
		if (id < 0) return;
		long now = System.nanoTime();
		if (now - lastActionNs < 250_000_000L) return; // de-duplicate event + polling paths
		lastActionNs = now;
		Config c = Config.INSTANCE;
		if (id == 0 || id == 1) { if (tab != id) { tab = id; layout(); } click(); }
		else if (id >= 10 && id < 10 + NS) {
			SoundStyle st = SoundStyle.values()[id - 10];
			c.style = st.name(); Config.save();
			pulse[id - 10] = 1f;
			Sounds.play(st, Sounds.Type.CLICK); // instant preview of the style you picked
		} else if (id == 20) { dragging = true; setVolumeFrom(mx); }
		else if (id >= 30 && id < 34) Sounds.play(Sounds.Type.values()[id - 30]); // CLICK, OPEN, CLOSE, HOVER
		else if (id >= 40 && id < 40 + toggles.size()) {
			Toggle t = toggles.get(id - 40);
			t.set.accept(!t.get.get()); Config.save(); click();
		} else if (id == 50 || id == 51) { click(); onClose(); }
	}

	private static boolean anyPressed() {
		var mh = Minecraft.getInstance().mouseHandler; // launchers may report a tap as the right/other button
		return mh.isLeftPressed() || mh.isRightPressed() || mh.isMiddlePressed();
	}

	private void setVolumeFrom(double mx) {
		float v = (float) Math.max(0, Math.min(1, (mx - (sx + 6)) / (double) (sw - 12)));
		Config.INSTANCE.volume = Math.round(v * 20) / 20f; // 5% steps
		int step = Math.round(Config.INSTANCE.volume * 20);
		if (step != lastTickStep) { lastTickStep = step; Sounds.slide(Config.INSTANCE.volume); }
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		if (e.key() == InputConstants.KEY_N) { onClose(); return true; }
		return super.keyPressed(e);
	}

	@Override
	public void onClose() {
		Config.save();
		Minecraft.getInstance().gui.setScreen(parent);
	}

	// ---------------------------------------------------------------- drawing helpers
	private static void rr(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int c) {
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
		for (int j = 0; j < h; j++) {
			int inset = 0;
			if (j < r) { double d = r - j - 0.5; inset = r - (int) Math.round(Math.sqrt(r * r - d * d)); }
			else if (j >= h - r) { double d = j - (h - r) + 0.5; inset = r - (int) Math.round(Math.sqrt(r * r - d * d)); }
			g.fill(x + inset, y + j, x + w - inset, y + j + 1, lerp(top, bot, j / (float) Math.max(1, h - 1)));
		}
	}

	private void textC(GuiGraphicsExtractor g, String s, int cx, int y, int color) {
		Font f = getFont();
		g.text(f, s, cx - f.width(s) / 2, y, color);
	}

	private static float ease(float t) { t = Math.max(0, Math.min(1, t)); return 1 - (1 - t) * (1 - t) * (1 - t); }

	// ---------------------------------------------------------------- rendering
	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
		g.fillGradient(0, 0, this.width, this.height, 0xD0140F2E, 0xEE08061A);
		float t = (System.nanoTime() - startNs) / 1e9f;
		for (int i = 0; i < 22; i++) { // drifting sparkles (like the icon)
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

		// slider drag: follow the pointer while the button is held
		if (dragging) {
			if (anyPressed()) setVolumeFrom(mx);
			else { dragging = false; lastTickStep = -1; Config.save(); }
		}

		// fallback click path: detect the press ourselves (de-duplicated in handleClick)
		boolean down = anyPressed();
		if (down && !wasDown) handleClick(mx, my, 0);
		wasDown = down;

		// hover + animation state
		int h = hitAt(mx, my);
		if (h != hoverId) {
			hoverId = h;
			if (h >= 0 && c.hoverSound) Sounds.play(Sounds.Type.HOVER);
		}
		float k = Math.min(1f, dt * 14f);
		tabPos += (tab - tabPos) * k;
		for (int i = 0; i < NS; i++) {
			hoverT[i] += ((hoverId == 10 + i ? 1f : 0f) - hoverT[i]) * k;
			pulse[i] = Math.max(0, pulse[i] - dt * 3.5f);
		}
		for (int i = 0; i < toggles.size(); i++)
			swT[i] += ((toggles.get(i).get().get() ? 1f : 0f) - swT[i]) * k;

		int oy = c.animations ? (int) ((1 - ease(t / 0.3f)) * 16) : 0;

		// ---- card shell
		rr(g, px - 1, py + 2 + oy, pw + 2, ph + 2, 6, 0x55000000);
		rrGrad(g, px - 1, py - 1 + oy, pw + 2, ph + 2, 6, MINT, PINK);
		rr(g, px, py + oy, pw, ph, 5, CARD);
		g.fillGradient(px + 1, py + 5 + oy, px + pw - 1, py + headerH - 2 + oy, 0xFF2E2766, CARD);
		g.fill(px + 5, py + 1 + oy, px + pw - 5, py + 5 + oy, 0xFF2E2766);

		// ---- header: logo + title
		Icons.logo(g, px + 10, py + (headerH - 32) / 2 + oy, 1);
		int tx = px + 48, ty = py + headerH / 2 - 10 + oy;
		Component title = Component.literal("Soft Chime UI").withStyle(ChatFormatting.BOLD);
		g.text(font, title, tx + 1, ty + 1, 0xFFB03A78);
		g.text(font, title, tx, ty, YEL);
		g.text(font, "UI sounds & click effects  v" + VERSION, tx, ty + 12, MUTED);
		// close button
		Hit cl = hit(51);
		if (cl != null) {
			rr(g, cl.x, cl.y + oy, 16, 16, 4, hoverId == 51 ? PINK : CARD3);
			for (int i = 0; i < 6; i++) {
				g.fill(cl.x + 5 + i, cl.y + 5 + i + oy, cl.x + 6 + i, cl.y + 6 + i + oy, WHITE);
				g.fill(cl.x + 10 - i, cl.y + 5 + i + oy, cl.x + 11 - i, cl.y + 6 + i + oy, WHITE);
			}
		}

		// ---- tabs
		int ix = px + 12, iw = pw - 24, tabY = py + headerH + oy;
		int tw = (iw - 4) / 2;
		rr(g, ix, tabY, iw, 20, 5, DEEP);
		int indX = ix + 2 + Math.round(tabPos * (tw + 2));
		rr(g, indX, tabY + 2, tw, 16, 4, YEL);
		textC(g, "Sound", ix + tw / 2 + 1, tabY + 6, tab == 0 ? 0xFF2B2455 : MUTED);
		textC(g, "Effects", ix + tw + 2 + (iw - tw - 2) / 2, tabY + 6, tab == 1 ? 0xFF2B2455 : MUTED);

		int y = py + headerH + 28 + oy;
		if (tab == 0) drawSoundTab(g, font, t, y, oy);
		else drawEffectsTab(g, font, oy);

		// ---- footer
		Hit done = hit(50);
		if (done != null) {
			int dy = done.y + oy;
			rr(g, done.x, dy + 2, done.w, done.h, 5, 0xFFB5902A);
			rr(g, done.x, dy, done.w, done.h, 5, hoverId == 50 ? 0xFFFFE9A0 : YEL);
			textC(g, "Done", done.x + done.w / 2, dy + 7, 0xFF2B2455);
			if (hint) textC(g, "Press N anytime to open this menu", px + pw / 2, dy + done.h + 5, MUTED);
		}
	}

	private void drawSoundTab(GuiGraphicsExtractor g, Font font, float t, int y, int oy) {
		Config c = Config.INSTANCE;
		g.text(font, "SOUND STYLE", px + 12, y, MUTED);
		SoundStyle cur = c.style();
		for (int i = 0; i < NS; i++) {
			Hit hh = hit(10 + i);
			if (hh == null) continue;
			boolean sel = cur.ordinal() == i;
			int cx = hh.x, cy = hh.y + oy - (int) (hoverT[i] * 2), cw = hh.w, chh = hh.h;
			rr(g, cx, cy + 2, cw, chh, 5, 0x55000000);
			rr(g, cx, cy, cw, chh, 5, lerp(CARD2, CARD3, hoverT[i]));
			if (sel) {
				rr(g, cx - 1, cy - 1, cw + 2, chh + 2, 6, YEL);
				rr(g, cx, cy, cw, chh, 5, lerp(0xFF3B3470, 0xFF46397F, hoverT[i]));
			}
			// accent stripe on the left
			g.fill(cx + 2, cy + 5, cx + 4, cy + chh - 5, ACCENT[i]);
			// icon (16x16), slightly bounces when picked
			int bounce = (int) (Math.sin(pulse[i] * Math.PI) * 3);
			Icons.style(g, SoundStyle.values()[i], cx + 7, cy + (chh - 16) / 2 - bounce, 1, 255);
			g.text(font, NAMES[i], cx + 25, cy + (chh - 8) / 2, WHITE);
			if (sel) { // "now playing" equalizer
				for (int b = 0; b < 3; b++) {
					int bh = 2 + (int) (Math.abs(Math.sin(t * 6 + b * 1.7)) * 6);
					g.fill(cx + cw - 8 + b * 2, cy + chh - 4 - bh, cx + cw - 7 + b * 2, cy + chh - 4, ACCENT[i]);
				}
			}
		}
		// volume
		Hit vh = hit(20);
		if (vh != null) {
			int vy = vh.y + oy;
			g.text(font, "VOLUME", px + 12, vy, MUTED);
			String pct = Math.round(c.volume * 100) + "%";
			g.text(font, pct, px + pw - 12 - font.width(pct), vy, YEL);
			int ty = sy + oy, tw2 = sw;
			rr(g, sx, ty, tw2, 6, 3, DEEP);
			int kx = sx + 6 + Math.round(c.volume * (tw2 - 12));
			for (int i = 0; i < kx - sx; i++) g.fill(sx + i, ty + 1, sx + i + 1, ty + 5, lerp(MINT, YEL, i / (float) tw2));
			rr(g, kx - 6, ty - 3, 12, 12, 6, PINK);
			rr(g, kx - 5, ty - 2, 10, 10, 5, WHITE);
		}
		// preview chips
		String[] names = {"Click", "Open", "Close", "Hover"};
		for (int i = 0; i < 4; i++) {
			Hit ch = hit(30 + i);
			if (ch == null) continue;
			int cy = ch.y + oy;
			boolean hv = hoverId == 30 + i;
			rr(g, ch.x, cy, ch.w, ch.h, 5, hv ? CARD3 : CARD2);
			int tw3 = font.width(names[i]);
			int startX = ch.x + (ch.w - (tw3 + 10)) / 2;
			int[] tri = {1, 2, 3, 2, 1};
			for (int r = 0; r < 5; r++) g.fill(startX, cy + 6 + r, startX + tri[r], cy + 7 + r, MINT);
			g.text(font, names[i], startX + 10, cy + 5, WHITE);
		}
	}

	private void drawEffectsTab(GuiGraphicsExtractor g, Font font, int oy) {
		for (int i = 0; i < toggles.size(); i++) {
			Hit hh = hit(40 + i);
			if (hh == null) continue;
			int cy = hh.y + oy;
			boolean hv = hoverId == 40 + i;
			rr(g, hh.x, cy, hh.w, hh.h, 5, hv ? CARD3 : CARD2);
			g.text(font, toggles.get(i).label, hh.x + 8, cy + 7, WHITE);
			float s = swT[i];
			int sw2 = 26, shh = 12, sx2 = hh.x + hh.w - sw2 - 8, sy2 = cy + (hh.h - shh) / 2;
			rr(g, sx2, sy2, sw2, shh, 6, lerp(OFF, MINT, s));
			int kx = sx2 + 1 + Math.round(s * (sw2 - shh));
			rr(g, kx, sy2 + 1, shh - 2, shh - 2, 5, WHITE);
		}
	}
}
