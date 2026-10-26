package com.combatgods.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** Katana de Jun. Más adelante aparecerá al activar la ultimate y desaparecerá al terminar. */
public class KatanaItem extends SwordItem {

    public KatanaItem(Item.Properties properties) {
        super(Tiers.NETHERITE, 3, -2.0F, properties);
    }

    @Override
    public boolean canBeDepleted() {
        return false; // no se rompe
    }
}
