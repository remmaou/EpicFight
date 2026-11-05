package yesman.epicfight.nightfall;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/// Memasang skill dodge milik senjata (Shadow Step / Yamato Step) di slot dodge selama senjata dipegang,
/// lalu mengembalikan skill dodge sebelumnya.
public final class ShadowDodge {
	private ShadowDodge() {}
	
	private static final Map<UUID, Optional<Skill>> PREVIOUS = new ConcurrentHashMap<>();
	
	public static void tick(ServerPlayerPatch patch, boolean holdingShadow) {
		tick(patch, holdingShadow ? NightfallSkills.SHADOW_STEP.get() : null);
	}
	
	public static void tick(ServerPlayerPatch patch, Skill wanted) {
		if (wanted != null) {
			equip(patch, wanted);
		} else if (PREVIOUS.containsKey(patch.getOriginal().getUUID())) {
			restore(patch);
		}
	}
	
	private static boolean isOurs(Skill skill) {
		return skill != null && (skill == NightfallSkills.SHADOW_STEP.get() || skill == NightfallSkills.YAMATO_STEP.get());
	}
	
	public static void equip(ServerPlayerPatch patch, Skill step) {
		SkillContainer dodge = patch.getSkill(SkillSlots.DODGE);
		
		if (dodge == null || dodge.getSkill() == step) {
			return;
		}
		
		PREVIOUS.computeIfAbsent(patch.getOriginal().getUUID(), id -> Optional.ofNullable(isOurs(dodge.getSkill()) ? null : dodge.getSkill()));
		
		if (dodge.setSkill(step)) {
			sync(patch, dodge);
		}
	}
	
	public static void restore(ServerPlayerPatch patch) {
		Optional<Skill> previous = PREVIOUS.remove(patch.getOriginal().getUUID());
		
		if (previous == null) {
			return;
		}
		
		SkillContainer dodge = patch.getSkill(SkillSlots.DODGE);
		
		if (dodge != null && isOurs(dodge.getSkill())) {
			dodge.setSkill(previous.orElse(null));
			sync(patch, dodge);
		}
	}
	
	private static void sync(ServerPlayerPatch patch, SkillContainer container) {
		ServerPlayer player = patch.getOriginal();
		EpicFightNetworkManager.sendToPlayer(container.createSyncPacketToLocalPlayer(), player);
	}
}
