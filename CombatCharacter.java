package com.combatgods.character;

import com.combatgods.CombatGodsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public enum CombatCharacter {
    JUN("jun", new ResourceLocation(CombatGodsMod.MODID, "textures/gui/jun_icon.png"), true),
    SHINJI("shinji", new ResourceLocation(CombatGodsMod.MODID, "textures/gui/shinji_icon.png"), false); // sin habilidades todavía

    private final String id;
    private final ResourceLocation icon;
    private final boolean available;

    CombatCharacter(String id, @Nullable ResourceLocation icon, boolean available) {
        this.id = id;
        this.icon = icon;
        this.available = available;
    }

    public String id() { return id; }
    @Nullable public ResourceLocation icon() { return icon; }
    public boolean isAvailable() { return available; }

    public Component displayName() {
        return Component.translatable("character." + CombatGodsMod.MODID + "." + id);
    }

    @Nullable
    public static CombatCharacter byId(String id) {
        for (CombatCharacter c : values()) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }
}
