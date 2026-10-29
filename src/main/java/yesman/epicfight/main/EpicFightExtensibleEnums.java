package yesman.epicfight.main;

import net.minecraft.world.item.Rarity;

public class EpicFightExtensibleEnums {
	public static void initExtensibleEnums() {
		EpicFightRairity.init();
	}

	public static class EpicFightRairity {
		/// TODO(fabric): NeoForge added a green "epicfight:unique" Rarity through its enum-extension system.
		/// Vanilla/Fabric has no such mechanism, so UNCOMMON is used until a mixin-based enum extension is added.
		public static Rarity UNIQUE;

		private static void init() {
			UNIQUE = Rarity.UNCOMMON;
		}
	}
}
