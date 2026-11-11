package dev.grit.registry;

import dev.grit.Grit;
import dev.grit.item.SplintItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
	public static final Item SPLINT = new SplintItem(new Item.Settings().maxCount(8));

	private ModItems() {}

	public static void register() {
		Registry.register(Registries.ITEM, new Identifier(Grit.MOD_ID, "splint"), SPLINT);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(SPLINT);
		});
	}
}
