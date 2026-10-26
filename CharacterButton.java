package com.combatgods.client;

import com.combatgods.character.CombatCharacter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Botón normal de Minecraft (mismo fondo y color) con el icono a la izquierda
 * y el nombre del personaje a la derecha.
 */
public class CharacterButton extends Button {
    /** Tamaño en píxeles del archivo jun_icon.png. */
    private static final int ICON_TEX_SIZE = 128;

    private final CombatCharacter character;

    public CharacterButton(int x, int y, int width, int height, CombatCharacter character, OnPress onPress) {
        // Mensaje vacío: el texto lo dibujamos nosotros para poder poner el icono al lado.
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.character = character;
        this.active = character.isAvailable();
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(g, mouseX, mouseY, partialTick); // fondo del botón de Minecraft

        Font font = Minecraft.getInstance().font;
        int pad = 4;
        int iconSize = this.height - pad * 2;
        int textX = getX() + 8;

        ResourceLocation icon = character.icon();
        if (icon != null) {
            RenderSystem.enableBlend(); // respeta la transparencia del PNG
            g.blit(icon, getX() + pad, getY() + pad, iconSize, iconSize,
                    0, 0, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE);
            textX = getX() + pad + iconSize + 8;
        }

        MutableComponent name = character.displayName().copy();
        if (!character.isAvailable()) {
            name.append(" ").append(Component.translatable("character.combatgods.coming_soon"));
        }

        int color;
        if (!this.active) color = 0xA0A0A0;
        else if (ClientState.selected == character) color = 0xFFFF55;
        else if (isHoveredOrFocused()) color = 0xFFFFA0;
        else color = 0xFFFFFF;

        g.drawString(font, name, textX, getY() + (this.height - 8) / 2, color);
    }
}
