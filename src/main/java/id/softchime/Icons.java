package id.softchime;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Pixel-art icons drawn with plain fills (no textures needed). Generated. */
public final class Icons {
	private static final String PAL_KEYS = ".YOWPpMmCBDRrLGgKV";
	private static final int[] PAL_VALS = {0x00000000, 0xFFFFD866, 0xFFE0A030, 0xFFFFFFFF, 0xFFFF5FA2, 0xFFC03C80, 0xFF6BF5D0, 0xFF2FB8A0, 0xFF6BD6FF, 0xFF3AA0E8, 0xFF1E6FB8, 0xFFE8903A, 0xFFB5651D, 0xFFFFC27A, 0xFF7DFF8A, 0xFF2FB85A, 0xFF3A2F6B, 0xFFB48CFF};

	private static int color(char c) {
		int i = PAL_KEYS.indexOf(c);
		return i < 0 ? 0 : PAL_VALS[i];
	}

	/** 16x16 icon for a sound style. */
	public static void style(GuiGraphicsExtractor g, SoundStyle s, int x, int y, int scale, int alpha) {
		draw(g, ART[s.ordinal()], x, y, scale, alpha, true);
	}

	/** 32x32 mod logo. */
	public static void logo(GuiGraphicsExtractor g, int x, int y, int scale) {
		draw(g, LOGO, x, y, scale, 255, false);
	}

	private static void draw(GuiGraphicsExtractor g, String[] rows, int x, int y, int scale, int alpha, boolean style) {
		for (int j = 0; j < rows.length; j++) {
			String row = rows[j];
			int i = 0;
			while (i < row.length()) {
				char c = row.charAt(i);
				int k = i;
				while (k < row.length() && row.charAt(k) == c) k++;
				if (c != '.') {
					int col = style ? color(c) : LOGO_COLORS[logoIndex(c)];
					col = (col & 0xFFFFFF) | ((((col >>> 24) * alpha) / 255) << 24);
					g.fill(x + i * scale, y + j * scale, x + k * scale, y + (j + 1) * scale, col);
				}
				i = k;
			}
		}
	}

	private static int logoIndex(char c) { return LOGO_KEYS.indexOf(c); }

	private static final String LOGO_KEYS = "abcd";
	private static final int[] LOGO_COLORS = {0xFF78F8D0, 0xFF201840, 0xFFF8D858, 0xFFF848A0};

	private static final String[][] ART = {
		{
			"......MMMM......",
			".....MYYYYM...M.",
			".P..YYYYYYYO.M.M",
			"....YWYYYYYO..M.",
			"...YYWYYYYYYO...",
			"...YWYYYYYYYO...",
			"...YWYYYYYYYO...",
			"..YYWYYYYYYYYO..",
			"..YYYYYYYYYYYO..",
			".YYYYYYYYYYYYYO.",
			".YYYYYYYYYYYYYOO",
			"OOOOOOOOOOOOOOO.",
			"................",
			"..P...OOOO......",
			".....OYYYYO.....",
			"......OOOO......"
		},
		{
			"................",
			"..m........ppp..",
			".mmm......pWWPp.",
			".mMmm.....pWPPp.",
			"..mmBBBBBBpPPPp.",
			"...BBCCCCBBppp..",
			"..BBWWCCCCBB....",
			"..BCWCCCCCCB....",
			".BBCCCCCCCCBB..W",
			".BCCCCCCCCCCB...",
			".WBCCCCCCCCBB...",
			"..BCCCCCCCCB....",
			"..BBCCCCCCBB..W.",
			"...BBCCCCBB.....",
			"....BBBBBB......",
			"................"
		},
		{
			".............PP.",
			"...M.........PP.",
			"..M.M.........L.",
			"...M..........L.",
			".LLLL...........",
			".LRRr...........",
			".LRRr.LLLL......",
			".LRRr.LRRr......",
			".LRRr.LRRr.LLLL.",
			".LRRr.LRRr.LRRr.",
			".LRRr.LRRr.LRRr.",
			".LRRr.LRRr.LRRr.",
			".rrrr.rrrr.rrrr.",
			"rrrrrrrrrrrrrrrr",
			"KKKKKKKKKKKKKKKK",
			"................"
		},
		{
			"................",
			"..............W.",
			"....MMMMMMMM.WWW",
			"...MWWMMMMmmm.W.",
			"..MWMMMMMMMmmm..",
			".MMMMMMWmmmmmmm.",
			".MMMMMMWmmmmmmm.",
			"..MMMMMWmmmmmm..",
			"..MMMMMWmmmmmm..",
			"...MMMMWmmmmm...",
			"....MMMWmmmm....",
			".....MMMmmm.....",
			".P....MMmm......",
			"P.P....Mm.......",
			".P..............",
			"................"
		},
		{
			"................",
			".......P........",
			".......P........",
			"................",
			"....G.....G.....",
			".....G...G......",
			"....GGGGGGG.....",
			"...GG.GGG.GG....",
			"..GGGGGGGGGGG...",
			"..G.GGGGGGG.G...",
			"..g.g.....g.g...",
			".....gg.gg......",
			"................",
			"................",
			".VVVVVVVVVVVVVV.",
			"................"
		},
		{
			"................",
			".....pPpPpP.....",
			"...Pp......Pp...",
			"..Pp.PPPPPP.Pp..",
			"..p.PPP..PPP.P..",
			".p.PP......PP.P.",
			".P.PP.YYYY.PP.p.",
			".p.P..YWYY..P.P.",
			".P.P..YYYY..P.p.",
			".p.PP.YYYY.PP.P.",
			".P.PP......PP.p.",
			"..P.PPP..PPP.p..",
			"..pP.PPPPPP.pP..",
			"...pP......pP...",
			".....PpPpPp.....",
			"................"
		}
	};

	private static final String[] LOGO = {
			"...aaaaaaaaaaaaaaaaaaaaaaaaaa...",
			"..abbbbbbbbbbbbbbbbbbbbbbbbbba..",
			".abbbbbbbbbbbbbbbbbbbbbbbbbbbba.",
			"abbbbbbbbbbbbbbbbbbbbbbbbbbbbbba",
			"abbbbbbbbbbbbbbbbbbbbbbbbbbbbbba",
			"abbbbabbbbbbbbbbbbbbbbbbbbbbbbba",
			"abbbaaabbbbbbbbbbbbbbbccbbbbbbba",
			"abbbbabbbbbbbbbbbbbcccccdbbbbbba",
			"abbbbbbbbbbbbbbcccccccccdbbbbbba",
			"abbbbbbbbbbcccccccccccccdbbbbbba",
			"abbbbbbbbbbcccccccccddccdbbbbbba",
			"abbbbbbbbbbcccccdddddbccdbbbbbba",
			"abbbbbbbbbbccddddbbbbbccdbbabbba",
			"abbbbabbbbbccdbbbbbbbbccdbaaabba",
			"abbbaaabbbbccdbbbbbbbbccdbbabbba",
			"abbbbabbbbbccdbbbbbbbbccdbbbbbba",
			"abbbbbbbbbbccdbbbbbbbbccdbbbbbba",
			"abbbbbbbbbbccdbbbbccccccdbbbbbba",
			"abbbbbbbbbbccdbbbbccccccdbbbbbba",
			"abbbbbbbbbbccdbbbbccccccdbbbbbba",
			"abbbbbbccccccdbbbbccccccdbbbbbba",
			"abbbbbbccccccdbbbbccccccdbbbbbba",
			"abbbbbbccccccdbbbbbddddddbbbbbba",
			"abbbbbbccccccdbbbbbbbbbbbbbbbbba",
			"abbbbbbccccccdbbbbbbbbbbbbbbbbba",
			"abbbbbbbddddddbbbbbbbbbbbbbabbba",
			"abbbbbbbbbbbbbbbbbbbbbbbbbaaabba",
			"abbbbbbbbbbbbbbbbbbbbbbbbbbabbba",
			"abbbbbbbbbbbbbbbbbbbbbbbbbbbbbba",
			".abbbbbbbbbbbbbbbbbbbbbbbbbbbba.",
			"..abbbbbbbbbbbbbbbbbbbbbbbbbba..",
			"...aaaaaaaaaaaaaaaaaaaaaaaaaa..."
	};

	private Icons() {}
}
