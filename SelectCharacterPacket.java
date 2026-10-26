package com.combatgods.network;

import com.combatgods.character.CharacterData;
import com.combatgods.character.CombatCharacter;
import com.combatgods.skill.UltimateSystem;
import com.combatgods.state.CombatState;
import com.combatgods.skill.JunSkills;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: "quiero ser este personaje". */
public record SelectCharacterPacket(String id) {

    public static void encode(SelectCharacterPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.id(), 32);
    }

    public static SelectCharacterPacket decode(FriendlyByteBuf buf) {
        return new SelectCharacterPacket(buf.readUtf(32));
    }

    public static void handle(SelectCharacterPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            CombatCharacter character = CombatCharacter.byId(packet.id());
            if (character == null || !character.isAvailable()) return; // el servidor valida
            CharacterData.set(player, character);
            JunSkills.ensureWeapon(player); // Jun recibe su daga
            JunSkills.sync(player);
            player.sendSystemMessage(Component.translatable("message.combatgods.selected", character.displayName()));
            UltimateSystem.onSelect(player);
            CombatState.get(player).sync(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
