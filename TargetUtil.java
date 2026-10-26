package com.combatgods.skill;

import com.combatgods.entity.ShadowClone;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

public final class TargetUtil {
    private TargetUtil() {}

    /** Jugadores y mobs validos (no el propio jugador, ni sus clones, ni soportes de armadura). */
    public static boolean valid(ServerPlayer p, LivingEntity e) {
        if (e == p || !e.isAlive() || e.isSpectator() || e instanceof ArmorStand) return false;
        if (e instanceof ShadowClone c && p.getUUID().equals(c.getOwnerId())) return false;
        return true;
    }

    /** Objetivo mas cercano dentro del cono de vision del jugador. */
    public static LivingEntity front(ServerPlayer p, double maxDist, double minDot) {
        Vec3 look = p.getLookAngle();
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(maxDist), x -> valid(p, x))) {
            Vec3 to = e.getEyePosition().subtract(p.getEyePosition());
            double d = to.length();
            if (d > maxDist || d < 1.0E-4) continue;
            if (to.normalize().dot(look) < minDot) continue;
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    /** Objetivo de frente cuya distancia a la superficie sea como maximo 'gap' bloques. */
    public static LivingEntity frontWithin(ServerPlayer p, double gap) {
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        Vec3 look = p.getLookAngle();
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(gap + 1.0), x -> valid(p, x))) {
            if (!p.getBoundingBox().inflate(gap).intersects(e.getBoundingBox())) continue;
            Vec3 to = e.position().subtract(p.position());
            if (to.length() > 1.0E-4 && to.normalize().dot(look) < 0.35) continue;
            double d = p.distanceToSqr(e);
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    /** Objetivo mas cercano en cualquier direccion. */
    public static LivingEntity nearest(ServerPlayer p, double maxDist) {
        LivingEntity best = null;
        double bestD = maxDist * maxDist;
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(maxDist), x -> valid(p, x))) {
            double d = p.distanceToSqr(e);
            if (d <= bestD) { bestD = d; best = e; }
        }
        return best;
    }
}
