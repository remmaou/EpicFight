package yesman.epicfight.platform.fabric.shim.clientevent;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;

public class MovementInputUpdateEvent extends ClientEvent {
    private final Player player;
    private final Input input;

    public MovementInputUpdateEvent(Player player, Input input) { this.player = player; this.input = input; }

    public Player getEntity() { return this.player; }
    public Input getInput() { return this.input; }
}
