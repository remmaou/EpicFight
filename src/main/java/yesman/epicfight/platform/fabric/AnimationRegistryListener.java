package yesman.epicfight.platform.fabric;

import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;

/// Fabric entrypoint "epicfight:animations": addon mods implement this and list it in their fabric.mod.json
/// to register animations (replaces listening to AnimationRegistryEvent on the NeoForge mod bus).
public interface AnimationRegistryListener {
    void onRegisterAnimations(AnimationRegistryEvent event);
}
