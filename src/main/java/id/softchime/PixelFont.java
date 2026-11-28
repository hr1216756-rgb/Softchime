package id.softchime;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Chunky pixel font drawn from a texture atlas (used by the Action skin). */
final class PixelFont {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("softchime", "textures/gui/pf.png");
	private static final int CW = 8, CH = 10, COLS = 16, TW = 128, TH = 60;
	private static final int[] ADV = {5, 3, 6, 6, 6, 6, 6, 3, 6, 6, 6, 6, 3, 6, 3, 6, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 3, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, 6, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 6, 6};

	private PixelFont() {}

	static int width(String s) {
		int w = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			w += (c >= 32 && c < 127) ? ADV[c - 32] : 6;
		}
		return w;
	}

	static void draw(GuiGraphicsExtractor g, String s, int x, int y, int color) {
		int col = 0xFF000000 | (color & 0xFFFFFF);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < 32 || c >= 127) { x += 6; continue; }
			int n = c - 32;
			if (c != ' ') g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y - 1, (n % COLS) * CW, (n / COLS) * CH, CW, CH, CW, CH, TW, TH, col);
			x += ADV[n];
		}
	}
}
