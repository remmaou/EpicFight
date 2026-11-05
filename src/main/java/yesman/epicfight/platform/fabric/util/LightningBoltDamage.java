package yesman.epicfight.platform.fabric.util;

/// Pengganti field/method `LightningBolt#damage` / `setDamage` / `getDamage` yang ditambahkan NeoForge
/// (di vanilla damage petir selalu 5.0F). Diimplementasikan oleh MixinLightningBolt.
public interface LightningBoltDamage {
    void epicfight$setDamage(float damage);

    float epicfight$getDamage();
}
