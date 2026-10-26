package com.combatgods.skill;

import net.minecraft.world.entity.LivingEntity;

/**
 * Punto de enganche para la fase de Shinji: con la ultimate activa podrá esquivar cada corte
 * de la katana saltando justo a tiempo. Por ahora ningún corte se esquiva.
 */
public final class CutHooks {
    private CutHooks() {}

    public static boolean shouldDodge(LivingEntity target, int cutIndex) {
        return false;
    }
}
