package com.combatgods.skill;

import com.combatgods.character.CharacterData;
import com.combatgods.character.CombatCharacter;
import com.combatgods.config.CombatConfig;
import com.combatgods.item.ModItems;
import com.combatgods.state.CombatState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Barra de ultimate (se llena al pelear), activacion con G, duracion y cambio daga <-> katana. */
public final class UltimateSystem {
    private UltimateSystem() {}

    public static void tryActivate(ServerPlayer p, CombatState st) {
        if (st.ultTicks > 0) return;
        if (st.ultimate < 100f) { Hits.message(p, "ult_not_ready"); return; }
        st.ultTicks = CombatConfig.ULT_SECONDS.get() * 20;
        swap(p, ModItems.DAGA_JUN.get(), ModItems.KATANA_JUN.get());
        Hits.sound(p, p, SoundEvents.WITHER_SPAWN, 0.6f, 1.4f);
        Hits.particles(p, p, ParticleTypes.FLAME, 40, 0.8);
        Hits.message(p, "ult_on");
        st.dirty = true;
    }

    public static void end(ServerPlayer p, CombatState st) {
        st.ultTicks = 0;
        st.ultimate = 0f;
        st.counterTicks = 0;
        st.ultRushTicks = 0;
        KatanaSkill.cancel(p, st);
        swap(p, ModItems.KATANA_JUN.get(), ModItems.DAGA_JUN.get());
        Hits.message(p, "ult_off");
        st.dirty = true;
    }

    /** Al elegir a Jun o reaparecer: entrega la daga (o la katana si la ultimate sigue activa). */
    public static void onSelect(ServerPlayer p) {
        if (CharacterData.get(p) != CombatCharacter.JUN) return;
        CombatState st = CombatState.get(p);
        Item want = st.ultimateActive() ? ModItems.KATANA_JUN.get() : ModItems.DAGA_JUN.get();
        Item other = st.ultimateActive() ? ModItems.DAGA_JUN.get() : ModItems.KATANA_JUN.get();
        swap(p, other, want);
        st.sync(p);
    }

    /** Cambia todas las copias de 'from' por una de 'to'; si no habia ninguna, entrega 'to'. */
    public static void swap(ServerPlayer p, Item from, Item to) {
        Inventory inv = p.getInventory();
        boolean placed = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(to) && !placed) { placed = true; continue; }
            if (s.is(from)) {
                if (!placed) { inv.setItem(i, new ItemStack(to)); placed = true; }
                else inv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (!placed) {
            ItemStack give = new ItemStack(to);
            if (!inv.add(give)) p.drop(give, false);
        }
    }
}
