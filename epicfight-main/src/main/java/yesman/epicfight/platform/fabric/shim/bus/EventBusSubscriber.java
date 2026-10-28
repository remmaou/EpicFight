package yesman.epicfight.platform.fabric.shim.bus;

import net.fabricmc.api.EnvType;
import java.lang.annotation.*;

/// Marker only (see SubscribeEvent).
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
    String modid() default "";
    EnvType[] value() default {EnvType.CLIENT, EnvType.SERVER};
}
