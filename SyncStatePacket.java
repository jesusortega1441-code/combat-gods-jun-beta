package com.combatgods.network;

import com.combatgods.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: estado para la barra de ultimate y los cooldowns. */
public record SyncStatePacket(String character, float ultimate, int ultTicks, int rushTicks, int counterTicks, int[] cooldown) {

    public static void encode(SyncStatePacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.character(), 32);
        buf.writeFloat(p.ultimate());
        buf.writeVarInt(p.ultTicks());
        buf.writeVarInt(p.rushTicks());
        buf.writeVarInt(p.counterTicks());
        buf.writeVarIntArray(p.cooldown());
    }

    public static SyncStatePacket decode(FriendlyByteBuf buf) {
        return new SyncStatePacket(buf.readUtf(32), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarIntArray());
    }

    public static void handle(SyncStatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.apply(p)));
        ctx.get().setPacketHandled(true);
    }
}
