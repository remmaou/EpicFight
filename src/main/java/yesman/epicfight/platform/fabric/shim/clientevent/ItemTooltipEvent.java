package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemTooltipEvent extends ClientEvent {
    private final ItemStack stack;
    private final Player player;
    private final List<Component> toolTip;

    public ItemTooltipEvent(ItemStack stack, Player player, List<Component> toolTip) {
        this.stack = stack; this.player = player; this.toolTip = toolTip;
    }

    public ItemStack getItemStack() { return this.stack; }
    public Player getEntity() { return this.player; }
    public List<Component> getToolTip() { return this.toolTip; }
}
