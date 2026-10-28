package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.world.level.Level;

public abstract class LevelTickEvent extends ClientEvent {
    private final Level level;

    protected LevelTickEvent(Level level) { this.level = level; }

    public Level getLevel() { return this.level; }

    public static class Post extends LevelTickEvent {
        public Post(Level level) { super(level); }
    }
}
