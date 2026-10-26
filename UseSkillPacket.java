package com.combatgods.network;

import com.combatgods.skill.Skill;
import com.combatgods.skill.SkillHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: "quiero usar esta habilidad". */
public record UseSkillPacket(int skill) {

    public static void encode(UseSkillPacket p, FriendlyByteBuf buf) { buf.writeVarInt(p.skill()); }

    public static UseSkillPacket decode(FriendlyByteBuf buf) { return new UseSkillPacket(buf.readVarInt()); }

    public static void handle(UseSkillPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Skill[] all = Skill.values();
            if (player == null || p.skill() < 0 || p.skill() >= all.length) return;
            SkillHandler.use(player, all[p.skill()]);
        });
        ctx.get().setPacketHandled(true);
    }
}
