package com.combatgods.client;

import com.combatgods.character.CombatCharacter;
import com.combatgods.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/** Barra de ultimate y cooldowns de las habilidades (esquina inferior izquierda). */
public final class HudOverlay {
    private HudOverlay() {}

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || ClientState.selected != CombatCharacter.JUN) return;
        Font font = mc.font;

        int x = 8, w = 110, h = 8;
        int y = height - 22;

        // barra de ultimate
        boolean active = ClientState.ultimateTicksLeft > 0;
        float frac = active ? 1.0F : Math.min(1.0F, ClientState.bar / 100.0F);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, 0xFF2A2A2A);
        int color = active ? 0xFFFFAA00 : (frac >= 1.0F ? 0xFFFFD84A : 0xFFB02020);
        g.fill(x, y, x + (int) (w * frac), y + h, color);
        String label;
        if (active) label = Component.translatable("hud.combatgods.ultimate_time", ClientState.ultimateTicksLeft / 20).getString();
        else if (frac >= 1.0F) label = Component.translatable("hud.combatgods.ultimate_ready").getString();
        else label = (int) ClientState.bar + "%";
        g.drawString(font, label, x, y - 10, 0xFFFFFF);

        // cooldowns de habilidades que estén recargando
        int line = y - 22;
        for (Skill s : Skill.values()) {
            if (s == Skill.ULTIMATE) continue;
            int ticks = ClientState.cooldownTicks[s.ordinal()];
            if (ticks <= 0) continue;
            String text = Component.translatable("skill.combatgods." + s.id()).getString()
                    + ": " + String.format("%.1f", ticks / 20.0) + "s";
            g.drawString(font, text, x, line, 0xFFAAAA);
            line -= 10;
        }
    }
}
