package com.combatgods.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** Nunchuks de Shinji. Más adelante solo existirán mientras su ultimate esté activada. */
public class NunchucksItem extends SwordItem {

    public NunchucksItem(Item.Properties properties) {
        super(Tiers.IRON, 2, -1.2F, properties);
    }

    @Override
    public boolean canBeDepleted() {
        return false; // no se rompen
    }
}
