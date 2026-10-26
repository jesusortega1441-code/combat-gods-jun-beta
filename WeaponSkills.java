package com.combatgods.skill;

import com.combatgods.config.CombatConfig;
import com.combatgods.item.ModItems;
import com.combatgods.state.CombatState;
import com.combatgods.state.Scheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Habilidad del arma que tengas en la mano (tecla V): daga o katana. */
public final class WeaponSkills {
    private WeaponSkills() {}

    public static boolean use(ServerPlayer p, CombatState st) {
        ItemStack main = p.getMainHandItem();
        if (main.is(ModItems.DAGA_JUN.get())) return dagger(p);
        if (main.is(ModItems.KATANA_JUN.get())) {
            if (!st.ultimateActive()) { Hits.message(p, "need_ultimate"); return false; }
            return KatanaSkill.start(p, st);
        }
        Hits.message(p, "need_weapon");
        return false;
    }

    /** Cortes cortos a quemarropa y patada final. */
    private static boolean dagger(ServerPlayer p) {
        LivingEntity t = TargetUtil.frontWithin(p, CombatConfig.NORMAL_RANGE.get());
        if (t == null) { Hits.message(p, "no_target"); return false; }
        float part = CombatConfig.DMG_DAGGER.get().floatValue() / 5f;
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Scheduler.schedule(i * 4, () -> {
                Hits.hit(p, t, part, 0.1, 0.0);
                Hits.particles(p, t, ParticleTypes.SWEEP_ATTACK, 2, 0.4);
                Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 1.1f + 0.1f * idx);
            });
        }
        Scheduler.schedule(18, () -> {
            Hits.hit(p, t, part, 1.2, 0.3);
            Hits.particles(p, t, ParticleTypes.CRIT, 15, 0.4);
            Hits.sound(p, t, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1f, 1f);
        });
        return true;
    }
}
