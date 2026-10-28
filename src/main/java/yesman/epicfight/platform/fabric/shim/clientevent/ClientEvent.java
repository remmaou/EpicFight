package yesman.epicfight.platform.fabric.shim.clientevent;

/// Base of the client event shims (replaces NeoForge's Event/ICancellableEvent for client events).
public abstract class ClientEvent {
    private boolean canceled;

    public boolean isCanceled() { return this.canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }
    public void setCanceled() { this.canceled = true; }
}
