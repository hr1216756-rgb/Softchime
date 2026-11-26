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

/** Cursor ring, click ripple, click particles, hover glow, open animation, slider ticks. */
public final class UiFx {
	private record Ripple(float x, float y, long t0) {}
	private static final class Particle {
		float x, y, vx, vy; long t0; int rgb;
	}

	private static final List<Ripple> RIPPLES = new ArrayList<>();
	private static final List<Particle> PARTICLES = new ArrayList<>();
	private static final int[] PALETTE = {0xFFE066, 0xFFFFFF, 0x9AD1FF, 0xFFB3D9};

	public static final boolean REANIMATED = FabricLoader.getInstance().isModLoaded("reanimated");
	private static long openedAt = 0;
	private static boolean wasLeft, wasRight;
	private static AbstractWidget hovered;
	private static long hoverStart;
	private static float lastTickX = Float.NaN;

	public static void onScreenOpened() { openedAt = System.currentTimeMillis(); }

	private static int argb(int alpha, int rgb) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
	}

	/** Called after the screen has drawn itself (signature may differ slightly per Fabric API build). */
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
			int a = (int) (200 * p);
			g.outline(h.getX() - 1, h.getY() - 1, h.getWidth() + 2, h.getHeight() + 2, argb(a, 0xFFE066));
		}

		// 3) slider drag ticks
		boolean left = mc.mouseHandler.isLeftPressed();
		boolean right = mc.mouseHandler.isRightPressed();
		if (left && h instanceof AbstractSliderButton && cfg.hoverSound) {
			if (Float.isNaN(lastTickX) || Math.abs(mouseX - lastTickX) >= 6) {
				Sounds.play(Sounds.Type.TICK);
				lastTickX = mouseX;
			}
		} else if (!left) lastTickX = Float.NaN;

		// 4) click -> sound + ripple + particles (anywhere on screen)
		if ((left && !wasLeft) || (right && !wasRight)) {
			if (cfg.clickSound) Sounds.play(Sounds.Type.CLICK);
			if (cfg.clickParticles) {
				RIPPLES.add(new Ripple(mouseX, mouseY, now));
				for (int i = 0; i < 14; i++) {
					Particle pt = new Particle();
					double ang = Math.random() * Math.PI * 2;
					double sp = 18 + Math.random() * 42; // gui px per second
					pt.x = mouseX; pt.y = mouseY;
					pt.vx = (float) (Math.cos(ang) * sp);
					pt.vy = (float) (Math.sin(ang) * sp) - 14;
					pt.t0 = now;
					pt.rgb = PALETTE[(int) (Math.random() * PALETTE.length)];
					PARTICLES.add(pt);
				}
			}
		}
		wasLeft = left; wasRight = right;

		RIPPLES.removeIf(r -> now - r.t0 > 420);
		for (Ripple r : RIPPLES) {
			float p = (now - r.t0) / 420f;
			ring(g, r.x, r.y, 3 + p * 15, argb((int) (210 * (1 - p)), 0xFFFFFF));
		}
		PARTICLES.removeIf(p -> now - p.t0 > 600);
		for (Particle p : PARTICLES) {
			float t = (now - p.t0) / 1000f;
			float life = (now - p.t0) / 600f;
			float x = p.x + p.vx * t;
			float y = p.y + p.vy * t + 60 * t * t; // little gravity
			int size = life < 0.5f ? 2 : 1;
			g.fill((int) x, (int) y, (int) x + size, (int) y + size, argb((int) (255 * (1 - life)), p.rgb));
		}

		// 5) cursor ring that follows the mouse
		if (cfg.cursorRing) {
			float pulse = (left || right) ? 0.8f : 1f;
			ring(g, mouseX, mouseY, 5 * pulse, argb(190, 0xFFFFFF));
		}
	}

	private static void ring(GuiGraphicsExtractor g, float cx, float cy, float r, int color) {
		int steps = Math.max(12, (int) (r * 5));
		for (int i = 0; i < steps; i++) {
			double a = i * Math.PI * 2 / steps;
			int x = Math.round(cx + (float) Math.cos(a) * r);
			int y = Math.round(cy + (float) Math.sin(a) * r);
			g.fill(x, y, x + 1, y + 1, color);
		}
	}

	private UiFx() {}
}
