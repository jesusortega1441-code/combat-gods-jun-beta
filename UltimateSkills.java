package com.combatgods.skill;

import com.combatgods.config.CombatConfig;
import com.combatgods.state.CombatState;
import com.combatgods.state.Scheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Las 3 habilidades de ultimate y el contraataque (solo con la ultimate activa). */
public final class UltimateSkills {
    private UltimateSkills() {}

    private static boolean needUlt(ServerPlayer p, CombatState st) {
        if (!st.ultimateActive()) { Hits.message(p, "need_ultimate"); return true; }
        return false;
    }

    /** 1) Cerca de un jugador o mob: lo agarra, tajos en X y remate con estallido. */
    public static boolean ult1(ServerPlayer p, CombatState st) {
        if (needUlt(p, st)) return false;
        LivingEntity t = TargetUtil.nearest(p, CombatConfig.ULT_1_RANGE.get());
        if (t == null) { Hits.message(p, "no_target"); return false; }
        float total = CombatConfig.DMG_ULT_1.get().floatValue();
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 9, false, false));   // lo inmoviliza
        Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1f, 0.6f);
        Scheduler.schedule(10, () -> { Hits.hit(p, t, total * 0.15f, 0.0, 0.0); Hits.particles(p, t, ParticleTypes.SWEEP_ATTACK, 4, 0.5); Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 1.3f); });
        Scheduler.schedule(14, () -> { Hits.hit(p, t, total * 0.15f, 0.0, 0.0); Hits.particles(p, t, ParticleTypes.SWEEP_ATTACK, 4, 0.5); Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 1.5f); });
        Scheduler.schedule(18, () -> { Hits.hit(p, t, total * 0.10f, 0.0, 0.0); Hits.particles(p, t, ParticleTypes.CRIT, 20, 0.5); });
        Scheduler.schedule(40, () -> {
            Hits.hit(p, t, total * 0.60f, 2.5, 0.5);
            Hits.particles(p, t, ParticleTypes.EXPLOSION_EMITTER, 1, 0.0);
            Hits.sound(p, t, SoundEvents.GENERIC_EXPLODE, 1.2f, 0.9f);
        });
        return true;
    }

    /** 2) De frente: dagas, corte y golpe final con estallido de polvo. */
    public static boolean ult2(ServerPlayer p, CombatState st) {
        if (needUlt(p, st)) return false;
        LivingEntity t = TargetUtil.front(p, CombatConfig.ULT_2_RANGE.get(), 0.6);
        if (t == null) { Hits.message(p, "no_target"); return false; }
        float total = CombatConfig.DMG_ULT_2.get().floatValue();
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9, false, false));
        Hits.sound(p, p, SoundEvents.TRIDENT_THROW, 1f, 1.2f);
        for (int i = 0; i < 3; i++) {
            Scheduler.schedule(8 + 4 * i, () -> {
                Hits.hit(p, t, total * 0.10f, 0.0, 0.0);
                Hits.particles(p, t, ParticleTypes.CRIT, 8, 0.4);
                Hits.sound(p, t, SoundEvents.TRIDENT_HIT, 1f, 1.3f);
            });
        }
        Scheduler.schedule(30, () -> { Hits.hit(p, t, total * 0.2f, 0.0, 0.0); Hits.particles(p, t, ParticleTypes.SWEEP_ATTACK, 6, 0.6); Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 0.8f); });
        Scheduler.schedule(50, () -> {
            Hits.hit(p, t, total * 0.5f, 3.0, 0.6);
            Hits.particles(p, t, ParticleTypes.LARGE_SMOKE, 40, 0.8);
            Hits.particles(p, t, ParticleTypes.EXPLOSION_EMITTER, 1, 0.0);
            Hits.sound(p, t, SoundEvents.GENERIC_EXPLODE, 1.3f, 0.7f);
        });
        return true;
    }

    /** 3) Carrera: sale disparado y, si choca de frente con alguien, lo mata de un golpe con explosion. */
    public static boolean ult3(ServerPlayer p, CombatState st) {
        if (needUlt(p, st)) return false;
        st.ultRushTicks = CombatConfig.RUSH_MAX_TICKS.get();
        Hits.sound(p, p, SoundEvents.ENDER_DRAGON_FLAP, 1f, 1.5f);
        st.dirty = true;
        return true;
    }

    public static void tickRush(ServerPlayer p, CombatState st) {
        if (st.ultRushTicks <= 0) return;
        st.ultRushTicks--;
        Vec3 look = p.getLookAngle();
        Vec3 h = new Vec3(look.x, 0, look.z).normalize();
        double speed = CombatConfig.RUSH_SPEED.get();
        p.setDeltaMovement(h.x * speed, Math.max(p.getDeltaMovement().y, -0.1), h.z * speed);
        p.hurtMarked = true;
        Hits.particles(p, p, ParticleTypes.CLOUD, 3, 0.2);

        List<LivingEntity> hit = p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(0.9), x -> TargetUtil.valid(p, x));
        if (!hit.isEmpty()) {
            punch(p, hit.get(0));
            st.ultRushTicks = 0;
            return;
        }
        if (p.horizontalCollision) st.ultRushTicks = 0;
    }

    private static void punch(ServerPlayer p, LivingEntity t) {
        Hits.particles(p, t, ParticleTypes.EXPLOSION_EMITTER, 1, 0.0);
        Hits.sound(p, t, SoundEvents.GENERIC_EXPLODE, 1.6f, 0.8f);
        t.invulnerableTime = 0;
        t.hurt(p.damageSources().playerAttack(p), Float.MAX_VALUE);           // un golpe = muerte
        double r = CombatConfig.RUSH_EXPLOSION_RADIUS.get();
        float dmg = CombatConfig.RUSH_EXPLOSION_DAMAGE.get().floatValue();
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, t.getBoundingBox().inflate(r), x -> TargetUtil.valid(p, x) && x != t)) {
            e.hurt(p.damageSources().explosion(p, p), dmg);                     // el jugador que golpeo no entra aqui
            Vec3 d = e.position().subtract(t.position());
            d = new Vec3(d.x, 0, d.z);
            if (d.lengthSqr() > 1.0E-4) d = d.normalize();
            e.setDeltaMovement(d.x * 1.2, 0.5, d.z * 1.2);
            e.hurtMarked = true;
        }
    }

    /** Contraataque: abre una ventana; si te golpean dentro de ella, el atacante muere de un golpe. */
    public static boolean counter(ServerPlayer p, CombatState st) {
        if (needUlt(p, st)) return false;
        st.counterTicks = (int) Math.round(CombatConfig.COUNTER_WINDOW_SECONDS.get() * 20.0);
        Hits.sound(p, p, SoundEvents.SHIELD_BLOCK, 0.8f, 1.4f);
        st.dirty = true;
        return true;
    }

    public static void doCounter(ServerPlayer p, LivingEntity attacker) {
        Hits.particles(p, attacker, ParticleTypes.CRIT, 40, 0.6);
        Hits.sound(p, attacker, SoundEvents.PLAYER_ATTACK_CRIT, 1.5f, 0.6f);
        attacker.invulnerableTime = 0;
        attacker.hurt(p.damageSources().playerAttack(p), Float.MAX_VALUE);     // le quita toda la vida
    }
}
