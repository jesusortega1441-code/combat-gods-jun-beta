package com.combatgods.skill;

/** Habilidades de Jun. Cada una tiene su tecla (cambiable en Controles de Minecraft). */
public enum Skill {
    NORMAL_1("normal_1", 90),   // Z
    NORMAL_2("normal_2", 88),   // X
    NORMAL_3("normal_3", 67),   // C
    CLONES("clones", 75),       // K
    WEAPON("weapon", 86),       // V  (daga o katana, la que tengas en la mano)
    ULT_1("ult_1", 66),         // B
    ULT_2("ult_2", 78),         // N
    ULT_3("ult_3", 77),         // M
    COUNTER("counter", 82),     // R
    ULTIMATE("ultimate", 71);   // G

    private final String id;
    private final int defaultKey;

    Skill(String id, int defaultKey) {
        this.id = id;
        this.defaultKey = defaultKey;
    }

    public String id() { return id; }
    public int defaultKey() { return defaultKey; }
}
