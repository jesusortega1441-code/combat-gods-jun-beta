package com.combatgods.network;

import com.combatgods.client.ClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: barra de ultimate, tiempo de ultimate y cooldowns (para el HUD). */
public record PlayerStatePacket(String character, float bar, int ultimateTicksLeft, int[] cooldownTicks) {

    public static void encode(PlayerStatePacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.character(), 32);
        buf.writeFloat(p.bar());
        buf.writeVarInt(p.ultimateTicksLeft());
        buf.writeVarInt(p.cooldownTicks().length);
        for (int c : p.cooldownTicks()) buf.writeVarInt(c);
    }

    public static PlayerStatePacket decode(FriendlyByteBuf buf) {
        String character = buf.readUtf(32);
        float bar = buf.readFloat();
        int ult = buf.readVarInt();
        int n = buf.readVarInt();
        int[] cd = new int[n];
        for (int i = 0; i < n; i++) cd[i] = buf.readVarInt();
        return new PlayerStatePacket(character, bar, ult, cd);
    }

    public static void handle(PlayerStatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientState.update(p.character(), p.bar(), p.ultimateTicksLeft(), p.cooldownTicks()));
        ctx.get().setPacketHandled(true);
    }
}
