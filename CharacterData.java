package com.combatgods.character;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Guarda el personaje elegido en los datos persistentes del jugador. */
public final class CharacterData {
    public static final String KEY = "combatgods_character";

    private CharacterData() {}

    @Nullable
    public static CombatCharacter get(Player player) {
        return CombatCharacter.byId(player.getPersistentData().getString(KEY));
    }

    public static void set(Player player, CombatCharacter character) {
        player.getPersistentData().putString(KEY, character.id());
    }
}
