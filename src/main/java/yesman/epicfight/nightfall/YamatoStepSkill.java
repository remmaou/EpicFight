package yesman.epicfight.nightfall;

import yesman.epicfight.skill.dodge.DodgeSkill;

/// Dodge Yamato (animasi dodge asli). Otomatis terpasang di slot dodge saat memegang Yamato.
public class YamatoStepSkill extends DodgeSkill {
	public YamatoStepSkill(DodgeSkill.Builder<?> builder) {
		super(builder);
	}
}
