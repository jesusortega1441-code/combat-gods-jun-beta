package com.combatgods.events;

import com.combatgods.CombatGodsMod;
import com.combatgods.entity.ModEntities;
import com.combatgods.entity.ShadowClone;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CombatGodsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SHADOW_CLONE.get(), ShadowClone.createAttributes().build());
    }
}
