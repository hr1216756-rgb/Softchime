package id.softchime;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Chunky pixel font drawn from a texture atlas (used by the Action skin). */
final class PixelFont {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("softchime", "textures/gui/pf.png");
	private static final int CW = 11, CH = 12, COLS = 16, TW = 176, TH = 72;
	private static final int[] ADV = {3, 4, 5, 9, 7, 11, 10, 3, 5, 5, 7, 9, 4, 5, 4, 6, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 4, 4, 8, 8, 8, 6, 11, 10, 8, 8, 9, 7, 7, 9, 9, 4, 5, 8, 7, 10, 9, 9, 8, 9, 9, 8, 8, 9, 10, 11, 9, 10, 8, 5, 6, 5, 8, 7, 4, 8, 8, 7, 8, 8, 7, 8, 8, 4, 5, 8, 4, 11, 8, 8, 8, 8, 6, 7, 7, 8, 8, 10, 8, 8, 6, 7, 3, 7, 8};

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
			if (c != ' ') g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y - 2, (n % COLS) * CW, (n / COLS) * CH, CW, CH, CW, CH, TW, TH, col);
			x += ADV[n];
		}
	}
}
