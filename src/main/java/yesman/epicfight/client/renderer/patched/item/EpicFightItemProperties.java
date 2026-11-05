package yesman.epicfight.client.renderer.patched.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.item.SkillBookItem;

public class EpicFightItemProperties {
	public static void registerItemProperties() {
		ItemProperties.register(EpicFightItems.SKILLBOOK.get(), EpicFightMod.identifier("skill"), (itemstack, level, entity, i) -> {
            Holder<Skill> skill = SkillBookItem.getContainSkill(itemstack).orElse(null);

            if (skill != null) {
                ResourceLocation icon = skill.value().getCategory().bookIcon();

                // Index tetap yang dipetakan ke "overrides" di assets/epicfight/models/item/skillbook.json.
                // Kategori tambahan dari addon memakai ikon default (0).
                if (icon.getNamespace().equals(EpicFightMod.MODID)) {
                    return switch (icon.getPath()) {
                        case "skillbook_dodge" -> 1.0F;
                        case "skillbook_passive" -> 2.0F;
                        case "skillbook_guard" -> 3.0F;
                        case "skillbook_mover" -> 4.0F;
                        case "skillbook_identity" -> 5.0F;
                        default -> 0.0F;
                    };
                }

                return 0.0F;
            }

            return 0.0F;
        });
	}
}