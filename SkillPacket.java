package com.combatgods.network;

import com.combatgods.skill.JunSkills;
import com.combatgods.skill.Skill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: "usé la habilidad N". */
public record SkillPacket(int skill) {

    public static void encode(SkillPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.skill());
    }

    public static SkillPacket decode(FriendlyByteBuf buf) {
        return new SkillPacket(buf.readVarInt());
    }

    public static void handle(SkillPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Skill skill = Skill.byOrdinal(packet.skill());
            if (skill == null) return; // el servidor valida todo
            JunSkills.use(player, skill);
        });
        ctx.get().setPacketHandled(true);
    }
}
