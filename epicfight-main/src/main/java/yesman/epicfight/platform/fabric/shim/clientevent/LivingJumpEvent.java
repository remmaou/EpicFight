package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.world.entity.LivingEntity;

public class LivingJumpEvent extends ClientEvent {
    private final LivingEntity entity;

    public LivingJumpEvent(LivingEntity entity) { this.entity = entity; }

    public LivingEntity getEntity() { return this.entity; }
}
