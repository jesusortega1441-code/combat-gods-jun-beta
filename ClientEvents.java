package com.combatgods.client;

import com.combatgods.CombatGodsMod;
import com.combatgods.network.ModNetwork;
import com.combatgods.network.UseSkillPacket;
import com.combatgods.skill.Skill;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;
import java.util.Map;

public class ClientEvents {
    private static final String CATEGORY = "key.categories.combatgods";

    /** Menu de personajes: H (la G ahora es de la ultimate). */
    public static final KeyMapping OPEN_SELECT = new KeyMapping("key.combatgods.select_character", 72, CATEGORY);

    /** Una tecla por habilidad, todas cambiables en Controles. */
    public static final Map<Skill, KeyMapping> KEYS = new EnumMap<>(Skill.class);
    static {
        for (Skill s : Skill.values()) {
            KEYS.put(s, new KeyMapping("key.combatgods.skill_" + s.id(), s.defaultKey(), CATEGORY));
        }
    }

    @Mod.EventBusSubscriber(modid = CombatGodsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_SELECT);
            for (KeyMapping k : KEYS.values()) event.register(k);
        }
    }

    @Mod.EventBusSubscriber(modid = CombatGodsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            while (OPEN_SELECT.consumeClick()) {
                if (mc.player != null && mc.screen == null) mc.setScreen(new CharacterSelectScreen());
            }
            for (Map.Entry<Skill, KeyMapping> e : KEYS.entrySet()) {
                while (e.getValue().consumeClick()) {
                    if (mc.player != null && mc.screen == null) {
                        ModNetwork.CHANNEL.sendToServer(new UseSkillPacket(e.getKey().ordinal()));
                    }
                }
            }
        }
    }
}
