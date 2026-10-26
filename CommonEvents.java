package com.combatgods.events;

import com.combatgods.CombatGodsMod;
import com.combatgods.character.CharacterData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CombatGodsMod.MODID)
public class CommonEvents {

    /** Al morir, el jugador conserva el personaje elegido. */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().putString(CharacterData.KEY,
                event.getOriginal().getPersistentData().getString(CharacterData.KEY));
    }
}
