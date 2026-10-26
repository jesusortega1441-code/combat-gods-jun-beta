package com.combatgods.skill;

import com.combatgods.CombatGodsMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Ejecuta tareas "dentro de N ticks" (para encadenar golpes, cortes y efectos). */
@Mod.EventBusSubscriber(modid = CombatGodsMod.MODID)
public final class ServerScheduler {
    private static final class Task {
        final long at; final Runnable run;
        Task(long at, Runnable run) { this.at = at; this.run = run; }
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0;

    private ServerScheduler() {}

    public static void later(int ticks, Runnable run) {
        TASKS.add(new Task(now + Math.max(0, ticks), run));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        now++;
        List<Task> due = new ArrayList<>();
        for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
            Task t = it.next();
            if (t.at <= now) { due.add(t); it.remove(); }
        }
        for (Task t : due) {
            try { t.run.run(); } catch (Exception ignored) { /* una tarea rota no debe tumbar el servidor */ }
        }
    }
}
