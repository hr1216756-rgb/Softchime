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

	private static int mix(int a, int b, float t) {
		int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
		int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
		int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
		return 0xFF000000 | (r << 16) | (g << 8) | bl;
	}

	private static final int[] TILE = {0xFF6A3FE0, 0xFF35B558, 0xFFE8478F, 0xFF22BFC9};

	/** Action-skin icon: bevelled coloured tile (like the reference menu) holding the style's own glyph. */
	public static void styleTile(GuiGraphicsExtractor g, SoundStyle st, int x, int y, int size) {
		int base = TILE[st.ordinal() % 4];
		int dark = mix(base, 0xFF000000, 0.62f); // dark body so the glyph colours always stand out
		g.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF120A22);
		g.fill(x, y, x + size, y + size, dark);
		g.fill(x, y, x + size, y + 1, base);
		g.fill(x, y, x + 1, y + size, base);
		g.fill(x, y + size - 1, x + size, y + size, mix(base, 0xFF000000, 0.5f));
		g.fill(x + size - 1, y, x + size, y + size, mix(base, 0xFF000000, 0.5f));
		String[] rows = ART[st.ordinal()];
		int in = size - 2; // glyph area: 16x16 art averaged down (box filter) so it stays readable
		for (int j = 0; j < in; j++) {
			int sy0 = j * 16 / in, sy1 = Math.max(sy0 + 1, (j + 1) * 16 / in);
			for (int i = 0; i < in; i++) {
				int sx0 = i * 16 / in, sx1 = Math.max(sx0 + 1, (i + 1) * 16 / in);
				int n = 0, tot = 0, r = 0, gg = 0, bb = 0;
				for (int yy = sy0; yy < sy1 && yy < rows.length; yy++)
					for (int xx = sx0; xx < sx1; xx++) {
						tot++;
						char c = rows[yy].charAt(xx);
						if (c == '.') continue;
						int col = color(c);
						r += (col >> 16) & 255; gg += (col >> 8) & 255; bb += col & 255; n++;
					}
				if (n * 2 < tot || n == 0) continue;
				int col = 0xFF000000 | ((r / n) << 16) | ((gg / n) << 8) | (bb / n);
				g.fill(x + 1 + i, y + 1 + j, x + 2 + i, y + 2 + j, col);
			}
		}
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
		},
		{
			"................",
			".......V........",
			".......VV.......",
			".......V.V......",
			".......V........",
			"....P..V........",
			"..VVVVVVVVVV....",
			".VWWWWWWWWWWV...",
			".VYYYYYYYYYYV...",
			".VYOYOYYOYOYV...",
			".VYYYYYYYYYYV...",
			".VVVVVVVVVVVV...",
			".K..........K...",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			"................",
			"......C.........",
			".....CCC........",
			".....CCC........",
			"....CCCCC.......",
			"....CWCCCC......",
			"...CWCCCCCC.....",
			"...CWCCCCCB.....",
			"...CCCCCCBB.....",
			"....CCCCBB......",
			".....BBBB.......",
			"................",
			"..BBBBBBBBBB....",
			"...DDDDDDDD.....",
			"................"
		}
		,{
			"..OOOOO.........",
			".OOYYYYOO.......",
			".OY.....YOO.....",
			".OY.W.W.W.YOO...",
			".OY.W.W.W..YO...",
			".OY.W.W.W..YO...",
			".OY.W.W.W..YO...",
			".OY.W.W.W.YOO...",
			".OY.W.W.W.YO....",
			".OY.W.W.WYOO....",
			".OOOWWWWWOO.....",
			"..OOOOOOOO......",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			"..W.W.W.W.W.W...",
			"..W.W.W.W.W.W...",
			".WW.W.W.W.W.WW..",
			".WW.W.W.W.W.WW..",
			".WW.W.W.W.W.WW..",
			"RRRRRRRRRRRRRRR.",
			"RLLLLLLLLLLLLLR.",
			"RLLLLLLLLLLLLLR.",
			"RLLLLLOOLLLLLLR.",
			"RLLLLLOOLLLLLLR.",
			"RLLLLLLLLLLLLLR.",
			"RRRRRRRRRRRRRRR.",
			"................",
			"................",
			"................"
		}
		,{
			".......W........",
			".......W........",
			"......WYW.......",
			"......WYW.......",
			".....WYYYW......",
			"WWWWWYYYYYWWWWW.",
			".....WYYYW......",
			"......WYW.......",
			"......WYW.......",
			".......W........",
			".......W.....Y..",
			"............YYY.",
			".....Y.......Y..",
			"....YYY.........",
			".....Y..........",
			"................"
		}
		,{
			"................",
			"KKKKKKKKKKKKKKKK",
			"KWWKKWWKKWWKKWWK",
			"KWWKKWWKKWWKKWWK",
			"KWWKKWWKKWWKKWWK",
			"KWWKKWWKKWWKKWWK",
			"KWWKKWWKKWWKKWWK",
			"KWWWKWWWKWWWKWWK",
			"KWWWKWWWKWWWKWWK",
			"KWWWKWWWKWWWKWWK",
			"KWWWKWWWKWWWKWWK",
			"KKKKKKKKKKKKKKKK",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			"................",
			"................",
			"..OOOOOOOOOOOO..",
			"..OYYYYYYYYYYO..",
			"...OYWYYYYYYO...",
			"...OYYYYYYYYO...",
			"....OYYYYYYO....",
			".....OOOOOO.....",
			"...PPPPPPPPPP...",
			"..PppppppppppP..",
			"................",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			".OOOOOOOOOOOOOO.",
			"OPPPPPPPPPPPPPPO",
			"OPWWPPPPPPPPPPPO",
			".OPPPPPPPPPPPPO.",
			".OPPPPPPPPPPPPO.",
			"..OPPPPPPPPPPO..",
			"...OPPPPPPPPO...",
			"....OPPPPPPO....",
			".....OPPPPO.....",
			"......OPPO......",
			".......OO.......",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			".......MM.......",
			"......MMMM......",
			".....MMMMMM.....",
			".....MWMMMM.....",
			"....MWMMMMMm....",
			"....MWMMMMMm....",
			"...MMMMMMMMmm...",
			"...MMMMMMMMmm...",
			"..MMMMMMMMMmmm..",
			"..mmmmmmmmmmmm..",
			".......WW.......",
			".....Y...Y......",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			"................",
			"K..............K",
			"KCC.CC.CC.CC.CCK",
			"KCC.CC.CC.CC.CCK",
			"KBB.BB.BB.BB.BBK",
			"KBB.BB.BB.BB.BBK",
			"K.B.BB.BB.BB.B.K",
			"K.B.BB.BB.B..B.K",
			"K..............K",
			"................",
			"................",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			"................",
			"..RRRRRRRRRRRR..",
			".RLLLLLLLLLLLLR.",
			".RLLLLLLLLLLLLR.",
			".RLLrrrrrrrrLLR.",
			".RLLrrrrrrrrLLR.",
			".RLLLLLLLLLLLLR.",
			".RLLLLLLLLLLLLR.",
			"..RRRRRRRRRRRR..",
			"................",
			"................",
			"................",
			"................",
			"................",
			"................"
		}
		,{
			"................",
			"................",
			"......VVV.......",
			"....VVVVVVV.....",
			"...VVWVVVVVVV...",
			"..VVWVVVVVVVVV..",
			".VVVVVVVVVVVVVV.",
			".VVVVVVVVVVVVVV.",
			"..VVVVVVVVVVVV..",
			"................",
			"................",
			"................",
			"................",
			"................",
			"................",
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
