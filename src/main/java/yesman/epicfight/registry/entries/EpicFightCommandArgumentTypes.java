package yesman.epicfight.registry.entries;

import yesman.epicfight.platform.fabric.util.FabricHolders;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import yesman.epicfight.platform.fabric.shim.registries.DeferredHolder;
import yesman.epicfight.platform.fabric.shim.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.server.commands.arguments.AnimationArgument;
import yesman.epicfight.server.commands.arguments.SkillArgument;

public final class EpicFightCommandArgumentTypes {
	private EpicFightCommandArgumentTypes() {}
	
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<SkillArgument, ?>> SKILL = REGISTRY.register("skill", () -> SingletonArgumentInfo.contextFree(SkillArgument::skill));
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<AnimationArgument, ?>> ANIMATION = REGISTRY.register("animation", () -> SingletonArgumentInfo.contextFree(AnimationArgument::animation));
	
	public static void registerArgumentTypes() {
		FabricHolders.registerByClass(SkillArgument.class, SKILL.get());
		FabricHolders.registerByClass(AnimationArgument.class, ANIMATION.get());
	}
}