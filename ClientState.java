package com.combatgods.client;

import com.combatgods.character.CombatCharacter;
import com.combatgods.skill.Skill;

public final class ClientState {
    /** Personaje elegido (viene del servidor). */
    public static CombatCharacter selected = null;
    public static float ultimate = 0f;
    public static int ultTicks = 0;
    public static int ultTotalTicks = 1;
    public static int rushTicks = 0;
    public static int counterTicks = 0;
    public static int[] cooldown = new int[Skill.values().length];

    private ClientState() {}
}
