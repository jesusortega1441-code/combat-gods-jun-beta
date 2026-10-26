package com.combatgods.events;

import com.combatgods.CombatGodsMod;
import com.combatgods.character.CharacterData;
import com.combatgods.character.CombatCharacter;
import com.combatgods.config.CombatConfig;
import com.combatgods.skill.KatanaSkill;
import com.combatgods.skill.UltimateSkills;
import com.combatgods.skill.UltimateSystem;
import com.combatgods.state.CombatState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CombatGodsMod.MODID)
public class ServerEvents {

    private static boolean isJun(ServerPlayer p) {
        return CharacterData.get(p) == CombatCharacter.JUN;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) return;
        if (!isJun(sp)) return;
        CombatState st = CombatState.get(sp);

        for (int i = 0; i < st.cooldown.length; i++) {
            if (st.cooldown[i] > 0) { st.cooldown[i]--; if (st.cooldown[i] % 20 == 0) st.dirty = true; }
        }
        if (st.ultTicks > 0) {
            st.ultTicks--;
            if (st.ultTicks == 0) UltimateSystem.end(sp, st);
            else if (st.ultTicks % 20 == 0) st.dirty = true;
        }
        if (st.counterTicks > 0) st.counterTicks--;

        KatanaSkill.tick(sp, st);
        UltimateSkills.tickRush(sp, st);

        if (st.dirty || sp.tickCount % 20 == 0) st.sync(sp);
    }

    /** La barra de ultimate se llena al golpear. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer sp && isJun(sp) && event.getEntity() != sp) {
            CombatState.get(sp).addUltimate(CombatConfig.ULT_FILL_PER_HIT.get().floatValue());
        }
    }

    /** Contraataque: si te golpean durante la ventana, el atacante muere y tu no recibes dano. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || !isJun(sp)) return;
        CombatState st = CombatState.get(sp);
        if (st.counterTicks <= 0 || !st.ultimateActive()) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != sp) {
            event.setCanceled(true);
            st.counterTicks = 0;
            st.dirty = true;
            UltimateSkills.doCounter(sp, attacker);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            CombatState st = CombatState.get(sp);
            KatanaSkill.cancel(sp, st);
            st.reset();
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) UltimateSystem.onSelect(sp);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            CombatState.get(sp).sync(sp);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CombatState.remove(event.getEntity().getUUID());
    }
}
