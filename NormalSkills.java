package com.combatgods.skill;

import com.combatgods.config.CombatConfig;
import com.combatgods.state.Scheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;

/** Habilidades normales 1, 2 y 3: mano vacia y objetivo de frente a 1 bloque. */
public final class NormalSkills {
    private NormalSkills() {}

    public static boolean use(ServerPlayer p, Skill s) {
        if (!p.getMainHandItem().isEmpty()) { Hits.message(p, "need_empty_hand"); return false; }
        LivingEntity t = TargetUtil.frontWithin(p, CombatConfig.NORMAL_RANGE.get());
        if (t == null) { Hits.message(p, "no_target"); return false; }
        if (s == Skill.NORMAL_1) normal1(p, t);
        else if (s == Skill.NORMAL_2) normal2(p, t);
        else if (s == Skill.NORMAL_3) normal3(p, t);
        else return false;
        return true;
    }

    /** Patadas en cadena y remate que lanza al rival hacia arriba. */
    private static void normal1(ServerPlayer p, LivingEntity t) {
        float part = CombatConfig.DMG_NORMAL_1.get().floatValue() / 4f;
        Hits.hit(p, t, part, 0.15, 0.05);
        Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_STRONG, 1f, 1f);
        Scheduler.schedule(6, () -> { Hits.hit(p, t, part, 0.15, 0.05); Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_STRONG, 1f, 1.1f); });
        Scheduler.schedule(12, () -> { Hits.hit(p, t, part, 0.15, 0.05); Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_STRONG, 1f, 1.2f); });
        Scheduler.schedule(20, () -> {
            Hits.hit(p, t, part, 0.3, 1.1);
            Hits.particles(p, t, ParticleTypes.CRIT, 20, 0.4);
            Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.2f, 0.9f);
        });
    }

    /** Golpe pesado con estallido y onda de choque. */
    private static void normal2(ServerPlayer p, LivingEntity t) {
        Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 0.7f);
        Scheduler.schedule(10, () -> {
            Hits.hit(p, t, CombatConfig.DMG_NORMAL_2.get().floatValue(), 1.6, 0.4);
            Hits.particles(p, t, ParticleTypes.EXPLOSION, 1, 0.0);
            Hits.sound(p, t, SoundEvents.GENERIC_EXPLODE, 0.7f, 1.2f);
        });
    }

    /** Patada, pisoton y patada final que manda al rival lejos. */
    private static void normal3(ServerPlayer p, LivingEntity t) {
        float total = CombatConfig.DMG_NORMAL_3.get().floatValue();
        Hits.hit(p, t, total * 0.2f, 0.1, 0.6);
        Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_STRONG, 1f, 0.9f);
        Scheduler.schedule(8, () -> {
            Hits.hit(p, t, total * 0.3f, 0.0, -0.6);
            Hits.particles(p, t, ParticleTypes.POOF, 15, 0.5);
            Hits.sound(p, t, SoundEvents.ANVIL_LAND, 0.6f, 1.4f);
        });
        Scheduler.schedule(16, () -> {
            Hits.hit(p, t, total * 0.5f, 2.0, 0.4);
            Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.2f, 0.8f);
        });
    }
}
