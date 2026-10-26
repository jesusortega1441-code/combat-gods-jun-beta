package com.combatgods.item;

import com.combatgods.CombatGodsMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CombatGodsMod.MODID);

    public static final RegistryObject<Item> KATANA_JUN = ITEMS.register("katana_jun",
            () -> new KatanaItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> DAGA_JUN = ITEMS.register("daga_jun",
            () -> new DaggerItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> NUNCHUKS_SHINJI = ITEMS.register("nunchuks_shinji",
            () -> new NunchucksItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<Item> PUNO_ACERO = ITEMS.register("puno_acero",
            () -> new Item(new Item.Properties().stacksTo(1).fireResistant()));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
