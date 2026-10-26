package com.combatgods.state;

import com.combatgods.CombatGodsMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Ejecuta acciones dentro de X ticks (para sincronizar golpes, cortes y efectos). */
public final class Scheduler {
    private static final List<Task> TASKS = new ArrayList<>();

    private static final class Task {
        int left;
        final Runnable action;
        Task(int left, Runnable action) { this.left = left; this.action = action; }
    }

    private Scheduler() {}

    public static void schedule(int ticks, Runnable action) {
        if (ticks <= 0) { action.run(); return; }
        TASKS.add(new Task(ticks, action));
    }

    @Mod.EventBusSubscriber(modid = CombatGodsMod.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) return;
            List<Task> due = new ArrayList<>();
            for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
                Task t = it.next();
                if (--t.left <= 0) { due.add(t); it.remove(); }
            }
            for (Task t : due) {
                try { t.action.run(); }
                catch (Exception ex) { CombatGodsMod.LOGGER.error("Error en una accion programada", ex); }
            }
        }

        @SubscribeEvent
        public static void onStopping(ServerStoppingEvent event) { TASKS.clear(); }
    }
}
