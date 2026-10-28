package yesman.epicfight.compat;

import org.jetbrains.annotations.NotNull;

/// Mods Epic Fight integrates with. The per-mod compatibility modules are not ported yet (see _pending/compat),
/// so this only keeps the identifiers other code asks for.
public enum MinecraftMod {
	SIMPLY_TOOLTIPS("simplytooltips", true);

	private final @NotNull String modId;
	private final boolean isClientOnly;

	MinecraftMod(@NotNull String modId, boolean isClientOnly) {
		this.modId = modId;
		this.isClientOnly = isClientOnly;
	}

	public @NotNull String getModId() {
		return this.modId;
	}

	public boolean isClientOnly() {
		return this.isClientOnly;
	}
}
