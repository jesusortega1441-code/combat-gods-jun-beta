package com.combatgods.skill;

import com.combatgods.config.CombatConfig;
import com.combatgods.state.CombatState;
import com.combatgods.state.Scheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Habilidad de la katana:
 *  1) Corre mas rapido (hasta 10 s). Con objetivo, corre hacia el.
 *  2) Segundo toque o fin del tiempo: atraviesa al objetivo, envaina y caen los cortes.
 *  3) Sin objetivo: frena suave y vuelve a la normalidad.
 */
public final class KatanaSkill {
    private static final UUID SPEED_ID = UUID.fromString("5f1c2a8e-7b8a-4c4a-9d57-0a1e5b6c7d01");

    private KatanaSkill() {}

    public static boolean start(ServerPlayer p, CombatState st) {
        st.rushTicks = CombatConfig.KATANA_RUSH_SECONDS.get() * 20;
        st.rushSlow = 0;
        LivingEntity t = TargetUtil.front(p, 24.0, 0.6);
        st.rushTarget = t == null ? null : t.getUUID();
        setSpeed(p, CombatConfig.KATANA_SPEED_BONUS.get());
        Hits.sound(p, p, SoundEvents.PLAYER_ATTACK_SWEEP, 0.8f, 0.6f);
        st.dirty = true;
        return true;
    }

    /** Segundo toque de la habilidad. */
    public static void secondPress(ServerPlayer p, CombatState st) {
        if (target(p, st) != null) strike(p, st);
        else endRush(p, st, true);
    }

    private static LivingEntity target(ServerPlayer p, CombatState st) {
        if (st.rushTarget == null) return null;
        Entity e = p.serverLevel().getEntity(st.rushTarget);
        if (e instanceof LivingEntity le && le.isAlive()) return le;
        return null;
    }

    public static void tick(ServerPlayer p, CombatState st) {
        if (st.rushSlow > 0) {
            st.rushSlow--;
            setSpeed(p, CombatConfig.KATANA_SPEED_BONUS.get() * st.rushSlow / 10.0);
            return;
        }
        if (st.rushTicks <= 0) return;
        st.rushTicks--;

        LivingEntity t = target(p, st);
        if (st.rushTarget != null && t == null) st.rushTarget = null; // el objetivo murio o se perdio

        if (t != null) {
            Vec3 d = t.position().subtract(p.position());
            double dist = Math.sqrt(d.x * d.x + d.z * d.z);
            if (dist <= 2.2) { strike(p, st); return; }
            p.setDeltaMovement(d.x / dist * 0.6, p.getDeltaMovement().y, d.z / dist * 0.6);
            p.hurtMarked = true;
        }
        if (st.rushTicks == 0) {
            if (t != null) strike(p, st);
            else endRush(p, st, true);
        }
        if (p.tickCount % 5 == 0) st.dirty = true;
    }

    private static void strike(ServerPlayer p, CombatState st) {
        LivingEntity t = target(p, st);
        endRush(p, st, false);
        if (t == null) return;

        Vec3 dir = t.position().subtract(p.position());
        dir = new Vec3(dir.x, 0, dir.z).normalize();
        Vec3 dest = t.position().add(dir.scale(2.5));
        Hits.particles(p, p, ParticleTypes.SWEEP_ATTACK, 6, 0.5);
        p.teleportTo(dest.x, t.getY(), dest.z);            // atraviesa al objetivo
        Hits.sound(p, p, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 0.7f);

        Scheduler.schedule(14, () -> Hits.sound(p, p, SoundEvents.IRON_DOOR_CLOSE, 0.8f, 1.6f)); // envainar

        int cuts = CombatConfig.KATANA_CUTS.get();
        float dmg = CombatConfig.KATANA_CUT_DAMAGE.get().floatValue();
        for (int i = 0; i < cuts; i++) {
            final int idx = i;
            Scheduler.schedule(24 + 6 * i, () -> {
                Hits.hit(p, t, dmg, 0.1, 0.05);
                Hits.particles(p, t, ParticleTypes.SWEEP_ATTACK, 3, 0.6);
                Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 1.2f + 0.05f * idx);
            });
        }
    }

    private static void endRush(ServerPlayer p, CombatState st, boolean soft) {
        st.rushTicks = 0;
        st.rushTarget = null;
        if (soft) st.rushSlow = 10;       // frena suave durante 10 ticks
        else { st.rushSlow = 0; setSpeed(p, 0); }
        st.dirty = true;
    }

    public static void cancel(ServerPlayer p, CombatState st) {
        st.rushTicks = 0; st.rushTarget = null; st.rushSlow = 0;
        setSpeed(p, 0);
    }

    private static void setSpeed(ServerPlayer p, double amount) {
        AttributeInstance a = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (a == null) return;
        a.removeModifier(SPEED_ID);
        if (amount > 0.001) {
            a.addTransientModifier(new AttributeModifier(SPEED_ID, "combatgods_rush", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
