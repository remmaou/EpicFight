package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;

public abstract class CustomizeGuiOverlayEvent extends ClientEvent {
    private final GuiGraphics guiGraphics;

    protected CustomizeGuiOverlayEvent(GuiGraphics guiGraphics) { this.guiGraphics = guiGraphics; }

    public GuiGraphics getGuiGraphics() { return this.guiGraphics; }

    public static class BossEventProgress extends CustomizeGuiOverlayEvent {
        private final LerpingBossEvent bossEvent;
        private final int x, y;

        public BossEventProgress(GuiGraphics guiGraphics, LerpingBossEvent bossEvent, int x, int y) {
            super(guiGraphics);
            this.bossEvent = bossEvent; this.x = x; this.y = y;
        }

        public LerpingBossEvent getBossEvent() { return this.bossEvent; }
        public int getX() { return this.x; }
        public int getY() { return this.y; }
    }
}
