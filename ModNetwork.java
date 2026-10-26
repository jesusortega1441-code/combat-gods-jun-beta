package com.combatgods.network;

import com.combatgods.CombatGodsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CombatGodsMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SelectCharacterPacket.class,
                SelectCharacterPacket::encode, SelectCharacterPacket::decode, SelectCharacterPacket::handle);
        CHANNEL.registerMessage(id++, UseSkillPacket.class,
                UseSkillPacket::encode, UseSkillPacket::decode, UseSkillPacket::handle);
        CHANNEL.registerMessage(id++, SyncStatePacket.class,
                SyncStatePacket::encode, SyncStatePacket::decode, SyncStatePacket::handle);
    }
}
