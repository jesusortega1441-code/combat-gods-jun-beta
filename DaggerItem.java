package com.combatgods.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** Daga de Jun. Más adelante solo existirá mientras la ultimate NO esté activada. */
public class DaggerItem extends SwordItem {

    public DaggerItem(Item.Properties properties) {
        super(Tiers.IRON, 1, -1.6F, properties);
    }

    @Override
    public boolean canBeDepleted() {
        return false; // no se rompe
    }
}
