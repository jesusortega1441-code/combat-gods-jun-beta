package com.combatgods.data;

import com.combatgods.skill.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/** Estado del jugador (barra, ultimate, cooldowns), guardado en sus datos persistentes. */
public final class PlayerData {
    public static final double BAR_MAX = 100.0;
    private static final String ROOT = "combatgods_data";

    private PlayerData() {}

    private static CompoundTag tag(Player p) {
        CompoundTag pd = p.getPersistentData();
        if (!pd.contains(ROOT, Tag.TAG_COMPOUND)) pd.put(ROOT, new CompoundTag());
        return pd.getCompound(ROOT);
    }

    public static double bar(Player p) { return tag(p).getDouble("bar"); }
    public static void setBar(Player p, double v) { tag(p).putDouble("bar", Math.max(0, Math.min(BAR_MAX, v))); }
    public static void addBar(Player p, double v) { setBar(p, bar(p) + v); }

    public static long ultEnd(Player p) { return tag(p).getLong("ult_end"); }
    public static void setUltEnd(Player p, long t) { tag(p).putLong("ult_end", t); }
    public static boolean ultActive(Player p) {
        long end = ultEnd(p);
        return end > 0 && p.level().getGameTime() < end;
    }

    public static long readyAt(Player p, Skill s) { return tag(p).getLong("ready_" + s.id()); }
    public static void setReadyAt(Player p, Skill s, long t) { tag(p).putLong("ready_" + s.id(), t); }

    public static long counterUntil(Player p) { return tag(p).getLong("counter_until"); }
    public static void setCounterUntil(Player p, long t) { tag(p).putLong("counter_until", t); }
}
