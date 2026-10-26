package com.combatgods.client;

import com.combatgods.character.CombatCharacter;
import com.combatgods.network.ModNetwork;
import com.combatgods.network.SelectCharacterPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CharacterSelectScreen extends Screen {
    private static final int BUTTON_W = 200;
    private static final int BUTTON_H = 36;
    private static final int GAP = 8;

    public CharacterSelectScreen() {
        super(Component.translatable("screen.combatgods.select_title"));
    }

    @Override
    protected void init() {
        CombatCharacter[] all = CombatCharacter.values();
        int total = all.length * BUTTON_H + (all.length - 1) * GAP;
        int x = this.width / 2 - BUTTON_W / 2;
        int y = this.height / 2 - total / 2 + 10;

        for (CombatCharacter character : all) {
            addRenderableWidget(new CharacterButton(x, y, BUTTON_W, BUTTON_H, character, btn -> {
                ClientState.selected = character;
                ModNetwork.CHANNEL.sendToServer(new SelectCharacterPacket(character.id()));
                onClose();
            }));
            y += BUTTON_H + GAP;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
