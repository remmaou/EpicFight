package yesman.epicfight.nightfall;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;

public final class NightfallSounds {
	private NightfallSounds() {}
	
	/// Memicu inisialisasi kelas (mendaftarkan holder).
	public static void init() {}
	
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_HEAVY_1 = register("whoosh_heavy_1");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_HEAVY_2 = register("whoosh_heavy_2");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_HEAVY_3 = register("whoosh_heavy_3");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_HEAVY_4 = register("whoosh_heavy_4");
	
	public static final DeferredHolder<SoundEvent, SoundEvent> YAMATO = register("yamato");
	public static final DeferredHolder<SoundEvent, SoundEvent> YAMATO_IN = register("yamato_in");
	public static final DeferredHolder<SoundEvent, SoundEvent> JUDGEMENTCUT = register("judgementcut");
	
	private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
		ResourceLocation location = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, name);
		return NightfallRegistries.SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
	}
}
