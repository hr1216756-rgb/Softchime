package id.softchime;

public enum SoundStyle {
	SOFT_CHIME("soft_chime", "Soft Chime (Default)"),
	BUBBLE_POP("bubble_pop", "Bubble Pop"),
	WOOD_MARIMBA("wood_marimba", "Wood Marimba"),
	CRYSTAL_GLASS("crystal_glass", "Crystal Glass"),
	RETRO_CHIP("retro_chip", "Retro Chip"),
	SOFT_TAP("soft_tap", "Soft Tap"),
	MUSIC_BOX("music_box", "Music Box"),
	WATER_DROP("water_drop", "Water Drop"),
	HARP("harp", "Harp"),
	KALIMBA("kalimba", "Kalimba"),
	STAR_SHIMMER("star_shimmer", "Star Shimmer"),
	SOFT_PIANO("soft_piano", "Soft Piano"),
	ZEN_BOWL("zen_bowl", "Zen Bowl"),
	GUITAR("guitar", "Gentle Guitar"),
	CELESTA("celesta", "Celesta"),
	VIBES("vibes", "Vibraphone"),
	WOOD("wood", "Wood Block"),
	PAD("pad", "Soft Pad");

	public final String id;
	public final String label;

	SoundStyle(String id, String label) { this.id = id; this.label = label; }

	public SoundStyle next() { return values()[(ordinal() + 1) % values().length]; }
	public SoundStyle prev() { return values()[(ordinal() + values().length - 1) % values().length]; }
}
