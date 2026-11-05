package yesman.epicfight.platform.fabric.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.List;

public final class DropCapture {
    private static final ThreadLocal<DropCapture> CURRENT = new ThreadLocal<>();

    public final LivingEntity owner;
    public final List<ItemEntity> drops = new ArrayList<>();

    private DropCapture(LivingEntity owner) {
        this.owner = owner;
    }

    public static DropCapture begin(LivingEntity owner) {
        DropCapture capture = new DropCapture(owner);
        CURRENT.set(capture);
        return capture;
    }

    public static DropCapture current() {
        return CURRENT.get();
    }

    public static void end() {
        CURRENT.remove();
    }

    private DropCapture() {
        this.owner = null;
    }
}
