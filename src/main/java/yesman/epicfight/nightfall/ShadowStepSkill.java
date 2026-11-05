package yesman.epicfight.nightfall;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/// Shadow Step: dash menghindar ala Genshin (kebal selama animasi), lalu tebasan berikutnya lebih cepat sesaat.
public class ShadowStepSkill extends DodgeSkill {
	public ShadowStepSkill(DodgeSkill.Builder<?> builder) {
		super(builder);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, CompoundTag args) {
		super.executeOnServer(container, args);
		
		ServerPlayerPatch executor = container.getServerExecutor();
		executor.getOriginal().addEffect(new MobEffectInstance(NightfallMobEffects.real(NightfallMobEffects.ATTACK_SPEED_INCREASE), 40, 30, false, false, true));
	}
}
