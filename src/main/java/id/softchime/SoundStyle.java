package id.softchime;

public enum SoundStyle {
	SOFT_CHIME("soft_chime", "Soft Chime (Default)"),
	BUBBLE_POP("bubble_pop", "Bubble Pop"),
	WOOD_MARIMBA("wood_marimba", "Wood Marimba"),
	CRYSTAL_GLASS("crystal_glass", "Crystal Glass"),
	RETRO_CHIP("retro_chip", "Retro Chip"),
	SOFT_TAP("soft_tap", "Soft Tap"),
	ASMR("asmr", "ASMR Whisper");

	public final String id;
	public final String label;

	SoundStyle(String id, String label) { this.id = id; this.label = label; }

	public SoundStyle next() { return values()[(ordinal() + 1) % values().length]; }
	public SoundStyle prev() { return values()[(ordinal() + values().length - 1) % values().length]; }
}
