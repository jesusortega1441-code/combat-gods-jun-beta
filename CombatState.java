package com.combatgods.state;

import com.combatgods.character.CharacterData;
import com.combatgods.character.CombatCharacter;
import com.combatgods.network.ModNetwork;
import com.combatgods.network.SyncStatePacket;
import com.combatgods.skill.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Estado temporal de cada jugador (cooldowns, ultimate, carreras). Vive solo en el servidor. */
public class CombatState {
    private static final Map<UUID, CombatState> STATES = new HashMap<>();

    public static CombatState get(Player p) {
        return STATES.computeIfAbsent(p.getUUID(), k -> new CombatState());
    }

    public static void remove(UUID id) { STATES.remove(id); }

    public float ultimate;                 // 0..100
    public int ultTicks;                   // ticks restantes de ultimate activa
    public final int[] cooldown = new int[Skill.values().length];
    public int rushTicks;                  // carrera de katana
    public int rushSlow;                   // frenado suave
    public UUID rushTarget;
    public int ultRushTicks;               // carrera de la habilidad 3 de ultimate
    public int counterTicks;               // ventana del contraataque
    public boolean dirty = true;

    public boolean ultimateActive() { return ultTicks > 0; }

    public void addUltimate(float v) {
        if (ultTicks > 0) return;
        ultimate = Math.min(100f, ultimate + v);
        dirty = true;
    }

    public void reset() {
        ultimate = 0; ultTicks = 0; rushTicks = 0; rushSlow = 0; rushTarget = null;
        ultRushTicks = 0; counterTicks = 0;
        java.util.Arrays.fill(cooldown, 0);
        dirty = true;
    }

    public void sync(ServerPlayer sp) {
        CombatCharacter c = CharacterData.get(sp);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                new SyncStatePacket(c == null ? "" : c.id(), ultimate, ultTicks, rushTicks, counterTicks, cooldown.clone()));
        dirty = false;
    }
}
