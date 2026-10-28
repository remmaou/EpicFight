package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.client.gui.GuiGraphics;

public abstract class RenderGuiEvent extends ClientEvent {
    private final GuiGraphics guiGraphics;

    protected RenderGuiEvent(GuiGraphics guiGraphics) { this.guiGraphics = guiGraphics; }

    public GuiGraphics getGuiGraphics() { return this.guiGraphics; }

    public static class Pre extends RenderGuiEvent {
        public Pre(GuiGraphics guiGraphics) { super(guiGraphics); }
    }
}
