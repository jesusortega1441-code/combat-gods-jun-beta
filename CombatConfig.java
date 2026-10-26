package com.combatgods.config;

import com.combatgods.skill.Skill;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.EnumMap;
import java.util.Map;

/** Opciones del mod (archivo config/combatgods-common.toml). */
public final class CombatConfig {
    public static final ForgeConfigSpec SPEC;
    private static final Map<Skill, ForgeConfigSpec.IntValue> COOLDOWN = new EnumMap<>(Skill.class);

    public static final ForgeConfigSpec.DoubleValue NORMAL_RANGE;
    public static final ForgeConfigSpec.DoubleValue DMG_NORMAL_1, DMG_NORMAL_2, DMG_NORMAL_3, DMG_DAGGER;
    public static final ForgeConfigSpec.DoubleValue DMG_ULT_1, DMG_ULT_2;
    public static final ForgeConfigSpec.IntValue ULT_SECONDS;
    public static final ForgeConfigSpec.DoubleValue ULT_FILL_PER_HIT, ULT_FILL_PER_SKILL;
    public static final ForgeConfigSpec.IntValue CLONES_NORMAL, CLONES_ULTIMATE, CLONE_EXTRA_HEARTS;
    public static final ForgeConfigSpec.IntValue KATANA_RUSH_SECONDS, KATANA_CUTS;
    public static final ForgeConfigSpec.DoubleValue KATANA_SPEED_BONUS, KATANA_CUT_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ULT_1_RANGE, ULT_2_RANGE;
    public static final ForgeConfigSpec.DoubleValue RUSH_SPEED, RUSH_EXPLOSION_RADIUS, RUSH_EXPLOSION_DAMAGE;
    public static final ForgeConfigSpec.IntValue RUSH_MAX_TICKS;
    public static final ForgeConfigSpec.DoubleValue COUNTER_WINDOW_SECONDS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Cooldown de cada habilidad, en segundos").push("cooldowns");
        for (Skill s : Skill.values()) {
            if (s == Skill.ULTIMATE) continue;
            COOLDOWN.put(s, b.defineInRange(s.id() + "_segundos", 20, 0, 600));
        }
        b.pop();

        b.push("habilidades_normales");
        NORMAL_RANGE = b.comment("Distancia maxima (bloques) al objetivo de frente").defineInRange("distancia", 1.0, 0.5, 6.0);
        DMG_NORMAL_1 = b.defineInRange("dano_normal_1", 8.0, 0.0, 1000.0);
        DMG_NORMAL_2 = b.defineInRange("dano_normal_2", 10.0, 0.0, 1000.0);
        DMG_NORMAL_3 = b.defineInRange("dano_normal_3", 12.0, 0.0, 1000.0);
        DMG_DAGGER = b.defineInRange("dano_daga", 8.0, 0.0, 1000.0);
        b.pop();

        b.push("ultimate");
        ULT_SECONDS = b.comment("Duracion de la ultimate").defineInRange("duracion_segundos", 120, 5, 3600);
        ULT_FILL_PER_HIT = b.comment("Cuanto se llena la barra (0-100) por golpe").defineInRange("carga_por_golpe", 4.0, 0.0, 100.0);
        ULT_FILL_PER_SKILL = b.comment("Cuanto se llena la barra (0-100) por habilidad").defineInRange("carga_por_habilidad", 8.0, 0.0, 100.0);
        ULT_1_RANGE = b.defineInRange("habilidad_1_distancia", 3.0, 1.0, 20.0);
        ULT_2_RANGE = b.defineInRange("habilidad_2_distancia", 6.0, 1.0, 30.0);
        DMG_ULT_1 = b.defineInRange("habilidad_1_dano", 24.0, 0.0, 1000.0);
        DMG_ULT_2 = b.defineInRange("habilidad_2_dano", 28.0, 0.0, 1000.0);
        RUSH_SPEED = b.comment("Velocidad de la carrera (bloques por tick)").defineInRange("carrera_velocidad", 1.4, 0.3, 4.0);
        RUSH_MAX_TICKS = b.defineInRange("carrera_max_ticks", 40, 5, 200);
        RUSH_EXPLOSION_RADIUS = b.defineInRange("explosion_radio", 4.0, 1.0, 20.0);
        RUSH_EXPLOSION_DAMAGE = b.defineInRange("explosion_dano", 12.0, 0.0, 1000.0);
        COUNTER_WINDOW_SECONDS = b.comment("Tiempo antes del golpe en que vale el contraataque").defineInRange("contraataque_segundos", 1.0, 0.1, 5.0);
        b.pop();

        b.push("clones");
        CLONES_NORMAL = b.defineInRange("cantidad_normal", 4, 1, 20);
        CLONES_ULTIMATE = b.defineInRange("cantidad_ultimate", 8, 1, 30);
        CLONE_EXTRA_HEARTS = b.comment("Corazones extra en ultimate").defineInRange("corazones_extra_ultimate", 5, 0, 50);
        b.pop();

        b.push("katana");
        KATANA_RUSH_SECONDS = b.defineInRange("carrera_segundos", 10, 1, 60);
        KATANA_SPEED_BONUS = b.comment("0.5 = 50% mas rapido").defineInRange("bonus_velocidad", 0.5, 0.0, 3.0);
        KATANA_CUTS = b.defineInRange("cantidad_cortes", 5, 1, 20);
        KATANA_CUT_DAMAGE = b.defineInRange("dano_por_corte", 6.0, 0.0, 1000.0);
        b.pop();

        SPEC = b.build();
    }

    private CombatConfig() {}

    public static int cooldownTicks(Skill s) {
        ForgeConfigSpec.IntValue v = COOLDOWN.get(s);
        return v == null ? 0 : v.get() * 20;
    }
}
