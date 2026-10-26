package com.combatgods.client;

import com.combatgods.character.CombatCharacter;
import com.combatgods.network.SyncStatePacket;

public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void apply(SyncStatePacket p) {
        ClientState.selected = CombatCharacter.byId(p.character());
        ClientState.ultimate = p.ultimate();
        if (p.ultTicks() > ClientState.ultTicks) ClientState.ultTotalTicks = p.ultTicks(); // al activarse
        ClientState.ultTicks = p.ultTicks();
        ClientState.rushTicks = p.rushTicks();
        ClientState.counterTicks = p.counterTicks();
        if (p.cooldown().length == ClientState.cooldown.length) ClientState.cooldown = p.cooldown();
    }
}
