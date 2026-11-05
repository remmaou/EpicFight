package yesman.epicfight.platform.fabric.shim.bus;

import java.lang.annotation.*;

/// Marker only. On Fabric these methods are NOT auto-registered; see platform/fabric for the real hooks.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;
    boolean receiveCanceled() default false;
}
