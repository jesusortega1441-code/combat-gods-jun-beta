package com.combatgods.skill;

import com.combatgods.config.CombatConfig;
import com.combatgods.entity.ModEntities;
import com.combatgods.entity.ShadowClone;
import com.combatgods.state.CombatState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

public final class ShadowClones {
    private ShadowClones() {}

    /** Invoca clones con la skin del jugador. Funciona a cualquier distancia y no tienen limite de tiempo. */
    public static boolean spawn(ServerPlayer p, CombatState st) {
        ServerLevel level = p.serverLevel();
        boolean ult = st.ultimateActive();
        int count = ult ? CombatConfig.CLONES_ULTIMATE.get() : CombatConfig.CLONES_NORMAL.get();
        for (int i = 0; i < count; i++) {
            ShadowClone c = ModEntities.SHADOW_CLONE.get().create(level);
            if (c == null) continue;
            double ox = (p.getRandom().nextDouble() * 2 - 1) * 2.5;
            double oz = (p.getRandom().nextDouble() * 2 - 1) * 2.5;
            c.moveTo(p.getX() + ox, p.getY(), p.getZ() + oz, p.getYRot(), 0);
            c.setOwner(p.getUUID());
            if (ult) c.boostHealth(CombatConfig.CLONE_EXTRA_HEARTS.get() * 2f);
            c.setPersistenceRequired();
            level.addFreshEntity(c);
            level.sendParticles(ParticleTypes.POOF, c.getX(), c.getY() + 1, c.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        }
        Hits.sound(p, p, SoundEvents.ENDERMAN_TELEPORT, 1f, 1.3f);
        return true;
    }
}
