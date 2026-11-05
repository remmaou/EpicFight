package yesman.epicfight.platform.fabric.shim.clientevent;

public enum TriState {
    FALSE, DEFAULT, TRUE;

    public boolean isTrue() { return this == TRUE; }
    public boolean isFalse() { return this == FALSE; }
    public boolean isDefault() { return this == DEFAULT; }
}
