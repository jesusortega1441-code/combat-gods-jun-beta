package com.combatgods.skill;

import com.combatgods.character.CharacterData;
import com.combatgods.character.CombatCharacter;
import com.combatgods.config.CombatConfig;
import com.combatgods.state.CombatState;
import net.minecraft.server.level.ServerPlayer;

/** Punto de entrada: el cliente pide usar una habilidad y aqui se valida y ejecuta. */
public final class SkillHandler {
    private SkillHandler() {}

    public static void use(ServerPlayer p, Skill s) {
        if (CharacterData.get(p) != CombatCharacter.JUN) return;       // por ahora solo Jun tiene habilidades
        CombatState st = CombatState.get(p);

        if (s == Skill.ULTIMATE) { UltimateSystem.tryActivate(p, st); return; }
        if (s == Skill.WEAPON && st.rushTicks > 0) { KatanaSkill.secondPress(p, st); return; } // segundo toque: sin cooldown nuevo

        int cd = st.cooldown[s.ordinal()];
        if (cd > 0) { Hits.message(p, "cooldown", (cd + 19) / 20); return; }

        boolean ok = switch (s) {
            case NORMAL_1, NORMAL_2, NORMAL_3 -> NormalSkills.use(p, s);
            case CLONES -> ShadowClones.spawn(p, st);
            case WEAPON -> WeaponSkills.use(p, st);
            case ULT_1 -> UltimateSkills.ult1(p, st);
            case ULT_2 -> UltimateSkills.ult2(p, st);
            case ULT_3 -> UltimateSkills.ult3(p, st);
            case COUNTER -> UltimateSkills.counter(p, st);
            default -> false;
        };
        if (ok) {
            st.cooldown[s.ordinal()] = CombatConfig.cooldownTicks(s);
            st.addUltimate(CombatConfig.ULT_FILL_PER_SKILL.get().floatValue());
            st.dirty = true;
        }
    }
}
