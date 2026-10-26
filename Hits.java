package com.combatgods.skill;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Golpes, sonidos y particulas. Los sonidos son de Minecraft por ahora (provisionales). */
public final class Hits {
    private Hits() {}

    public static void hit(ServerPlayer p, LivingEntity t, float dmg, double knock, double up) {
        if (t == null || !t.isAlive()) return;
        t.invulnerableTime = 0; // permite varios golpes seguidos
        t.hurt(p.damageSources().playerAttack(p), dmg);
        Vec3 d = t.position().subtract(p.position());
        d = new Vec3(d.x, 0, d.z);
        if (d.lengthSqr() > 1.0E-4) d = d.normalize();
        t.setDeltaMovement(d.x * knock, up, d.z * knock);
        t.hurtMarked = true;
    }

    public static void sound(ServerPlayer p, Entity at, SoundEvent s, float volume, float pitch) {
        p.level().playSound(null, at.getX(), at.getY(), at.getZ(), s, SoundSource.PLAYERS, volume, pitch);
    }

    public static void particles(ServerPlayer p, Entity at, ParticleOptions type, int count, double spread) {
        ((ServerLevel) p.level()).sendParticles(type, at.getX(), at.getY() + at.getBbHeight() / 2.0, at.getZ(),
                count, spread, spread, spread, 0.02);
    }

    public static void message(ServerPlayer p, String key, Object... args) {
        p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.combatgods." + key, args), true);
    }
}
