package com.combatgods;

import com.combatgods.config.CombatConfig;
import com.combatgods.entity.ModEntities;
import com.combatgods.item.ModItems;
import com.combatgods.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CombatGodsMod.MODID)
public class CombatGodsMod {
    public static final String MODID = "combatgods";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CombatGodsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CombatConfig.SPEC);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreative);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    /** Para probar: los ítems salen en la pestaña de combate del modo creativo. */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.KATANA_JUN);
            event.accept(ModItems.DAGA_JUN);
            event.accept(ModItems.NUNCHUKS_SHINJI);
            event.accept(ModItems.PUNO_ACERO);
        }
    }
}
