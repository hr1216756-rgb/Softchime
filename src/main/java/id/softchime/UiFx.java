package id.softchime;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/** Cursor ring, click ripple + sparkles, hover glow, open animation, slider ticks. */
public final class UiFx {
	private record Ripple(float x, float y, long t0) {}
	private static final class Particle {
		float x, y, vx, vy; long t0; int rgb;
	}

	private static final List<Ripple> RIPPLES = new ArrayList<>();
	private static final List<Particle> PARTICLES = new ArrayList<>();
	private static final int[] PALETTE = {0xFFD84A, 0xFFFFFF, 0xFFE9A0, 0xBFE3FF};

	public static final boolean REANIMATED = FabricLoader.getInstance().isModLoaded("reanimated");
	private static long openedAt = 0;
	private static AbstractWidget hovered;
	private static long hoverStart;
	private static float lastTickX = Float.NaN;

	public static void onScreenOpened() { openedAt = System.currentTimeMillis(); }

	private static int argb(int alpha, int rgb) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
	}

	private static int lerpRgb(int a, int b, float t) {
		int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
		int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
		int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
		return (r << 16) | (g << 8) | bl;
	}

	/** Fired for EVERY mouse press / screen tap (event based, so quick phone taps are never missed). */
	public static void onClick(Screen screen, double mx, double my, int button) {
		if (screen instanceof ConfigScreen cs) cs.handleClick(mx, my, button);
		Config cfg = Config.INSTANCE;
		long now = System.currentTimeMillis();
		// the settings screen plays its own sounds (so previewing a style plays the NEW style)
		if (cfg.clickSound && !(screen instanceof ConfigScreen)) Sounds.play(Sounds.Type.CLICK);
		if (cfg.clickParticles) {
			RIPPLES.add(new Ripple((float) mx, (float) my, now));
			for (int i = 0; i < 10; i++) {
				Particle pt = new Particle();
				double ang = Math.random() * Math.PI * 2;
				double sp = 24 + Math.random() * 36; // gui px per second
				pt.x = (float) mx; pt.y = (float) my;
				pt.vx = (float) (Math.cos(ang) * sp);
				pt.vy = (float) (Math.sin(ang) * sp) - 10;
				pt.t0 = now;
				pt.rgb = PALETTE[(int) (Math.random() * PALETTE.length)];
				PARTICLES.add(pt);
			}
		}
	}

	/** Called after the screen has drawn itself. */
	public static void render(Screen screen, GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		Minecraft mc = Minecraft.getInstance();
		Config cfg = Config.INSTANCE;
		long now = System.currentTimeMillis();
		// ReAnimated already animates screen open/close and hover -> don't draw duplicates on top of it.
		boolean ownAnim = cfg.animations && !(REANIMATED && cfg.reanimatedCompat);

		// 1) open animation: quick soft light flash that fades out
		if (ownAnim) {
			float p = (now - openedAt) / 260f;
			if (p >= 0 && p < 1) {
				int a = (int) (70 * (1 - p) * (1 - p));
				g.fill(0, 0, screen.width, screen.height, argb(a, 0xFFFFFF));
			}
		}

		// 2) hover detection (buttons, tabs, sliders, list entries ...)
		AbstractWidget h = null;
		for (var w : Screens.getWidgets(screen)) {
			if (w instanceof AbstractWidget aw && aw.visible && aw.active && aw.isMouseOver(mouseX, mouseY)) { h = aw; break; }
		}
		if (h != hovered) {
			hovered = h;
			hoverStart = now;
			if (h != null && cfg.hoverSound) Sounds.play(Sounds.Type.HOVER);
		}
		if (h != null && ownAnim) {
			float p = Math.min(1f, (now - hoverStart) / 140f);
			g.outline(h.getX() - 1, h.getY() - 1, h.getWidth() + 2, h.getHeight() + 2, argb((int) (200 * p), 0xFFE066));
		}

		// 3) slider drag ticks
		boolean left = mc.mouseHandler.isLeftPressed();
		if (left && h instanceof AbstractSliderButton && cfg.hoverSound) {
			if (Float.isNaN(lastTickX) || Math.abs(mouseX - lastTickX) >= 6) {
				Sounds.play(Sounds.Type.TICK);
				lastTickX = mouseX;
			}
		} else if (!left) lastTickX = Float.NaN;

		// 4) click ripple: thin ring that starts small + yellow, expands and fades to white (like the video)
		RIPPLES.removeIf(r -> now - r.t0 > 700);
		for (Ripple r : RIPPLES) {
			float t = (now - r.t0) / 520f;
			if (t < 1f) {
				float e = 1 - (1 - t) * (1 - t);
				ring(g, r.x, r.y, 2 + e * 24, argb((int) (235 * (1 - t)), lerpRgb(0xFFD84A, 0xFFFFFF, Math.min(1f, t * 3f))));
				if (t < 0.25f) g.fill((int) r.x - 1, (int) r.y - 1, (int) r.x + 1, (int) r.y + 1, argb(230, 0xFFD84A));
			}
			float t2 = (now - r.t0 - 90) / 480f;
			if (t2 > 0 && t2 < 1) {
				float e2 = 1 - (1 - t2) * (1 - t2);
				ring(g, r.x, r.y, 2 + e2 * 15, argb((int) (120 * (1 - t2)), 0xFFFFFF));
			}
		}
		PARTICLES.removeIf(p -> now - p.t0 > 560);
		for (Particle p : PARTICLES) {
			float t = (now - p.t0) / 1000f;
			float life = (now - p.t0) / 560f;
			float x = p.x + p.vx * t;
			float y = p.y + p.vy * t + 50 * t * t; // tiny gravity
			int size = life < 0.45f ? 2 : 1;
			g.fill((int) x, (int) y, (int) x + size, (int) y + size, argb((int) (255 * (1 - life)), p.rgb));
		}

		// 5) cursor ring that follows the pointer
		if (cfg.cursorRing) {
			float pulse = left ? 0.8f : 1f;
			ring(g, mouseX, mouseY, 6 * pulse, argb(170, 0xFFFFFF));
		}
	}

	private static void ring(GuiGraphicsExtractor g, float cx, float cy, float r, int color) {
		int steps = Math.max(16, (int) (r * 7));
		for (int i = 0; i < steps; i++) {
			double a = i * Math.PI * 2 / steps;
			int x = Math.round(cx + (float) Math.cos(a) * r);
			int y = Math.round(cy + (float) Math.sin(a) * r);
			g.fill(x, y, x + 1, y + 1, color);
		}
	}

	private UiFx() {}
}
