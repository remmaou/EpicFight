package yesman.epicfight.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.world.item.CreativeModeTab;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;

/// Lets an addon mod say which creative tab its skill books go into.
/// Addons call [#register(String, EpicFightExtensions)] from their mod initializer.
public record EpicFightExtensions(DeferredHolder<CreativeModeTab, CreativeModeTab> skillBookCreativeTab) {
	private static final Map<String, EpicFightExtensions> EXTENSIONS = new HashMap<> ();

	public static void register(String modId, EpicFightExtensions extension) {
		EXTENSIONS.put(modId, extension);
	}

	public static Optional<EpicFightExtensions> get(String modId) {
		return Optional.ofNullable(EXTENSIONS.get(modId));
	}
}
