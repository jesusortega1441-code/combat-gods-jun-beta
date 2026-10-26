package com.combatgods.skill;

import com.combatgods.entity.ShadowCloneEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class Targeting {
    private Targeting() {}

    public static boolean isOwnClone(ServerPlayer p, LivingEntity e) {
        return e instanceof ShadowCloneEntity c && c.isOwnedBy(p);
    }

    private static boolean valid(ServerPlayer p, LivingEntity e) {
        return e != p && e.isAlive() && !e.isSpectator() && !isOwnClone(p, e);
    }

    /**
     * Ser vivo más cercano que esté "de frente".
     * @param range  distancia en bloques desde el cuerpo del jugador
     * @param minDot qué tan de frente debe estar (1 = justo enfrente, 0 = a 90 grados)
     * @param needSight exige línea de visión (para distancias largas)
     */
    public static LivingEntity inFront(ServerPlayer p, double range, double minDot, boolean needSight) {
        AABB box = p.getBoundingBox().inflate(range);
        List<LivingEntity> list = p.level().getEntitiesOfClass(LivingEntity.class, box, e -> valid(p, e));
        Vec3 look = p.getLookAngle().normalize();
        Vec3 eye = p.getEyePosition();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : list) {
            Vec3 to = e.position().add(0, e.getBbHeight() / 2.0, 0).subtract(eye);
            double len = to.length();
            if (len < 1.0E-4) continue;
            if (to.scale(1.0 / len).dot(look) < minDot) continue;
            if (needSight && !p.hasLineOfSight(e)) continue;
            double d = p.distanceToSqr(e);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }

    /** Ser vivo más cercano en cualquier dirección. */
    public static LivingEntity nearest(ServerPlayer p, double range) {
        AABB box = p.getBoundingBox().inflate(range);
        List<LivingEntity> list = p.level().getEntitiesOfClass(LivingEntity.class, box, e -> valid(p, e));
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : list) {
            double d = p.distanceToSqr(e);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}
