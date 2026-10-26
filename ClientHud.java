package com.combatgods.client;

import com.combatgods.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Barra de ultimate y cooldowns, encima de la barra de objetos. */
public final class ClientHud {
    public static final IGuiOverlay OVERLAY = ClientHud::render;

    private ClientHud() {}

    private static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (ClientState.selected == null || mc.options.hideGui || mc.player == null) return;

        int x = width / 2 - 91;
        int y = height - 58;
        boolean active = ClientState.ultTicks > 0;
        float frac = active ? ClientState.ultTicks / (float) Math.max(1, ClientState.ultTotalTicks) : ClientState.ultimate / 100f;
        int color = active ? 0xFFFF8A00 : (ClientState.ultimate >= 100f ? 0xFFFFE04A : 0xFFB8860B);

        g.fill(x - 1, y - 1, x + 183, y + 5, 0xAA000000);
        g.fill(x, y, x + (int) (182 * Math.max(0f, Math.min(1f, frac))), y + 4, color);

        if (!active && ClientState.ultimate >= 100f) {
            String key = ClientEvents.KEYS.get(Skill.ULTIMATE).getTranslatedKeyMessage().getString();
            g.drawString(mc.font, "ULTIMATE [" + key + "]", x, y - 11, 0xFFFFE04A);
        } else if (active) {
            g.drawString(mc.font, (ClientState.ultTicks / 20) + "s", x, y - 11, 0xFFFF8A00);
        }

        int cx = x;
        for (Skill s : Skill.values()) {
            if (s == Skill.ULTIMATE) continue;
            int cd = ClientState.cooldown[s.ordinal()];
            if (cd <= 0) continue;
            String label = ClientEvents.KEYS.get(s).getTranslatedKeyMessage().getString() + " " + ((cd + 19) / 20) + "s";
            g.drawString(mc.font, label, cx, y + 8, 0xFFAAAAAA);
            cx += mc.font.width(label) + 8;
        }
    }
}
