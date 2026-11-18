package com.neolxsky.CBCAddition.client;

import com.neolxsky.CBCAddition.menu.InfinityCellConfiguratorMenu;
import com.neolxsky.CBCAddition.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 无限元件配置器 GUI：样品槽 → 产物槽 + 「制造」按钮。
 */
public class InfinityCellConfiguratorScreen extends AbstractContainerScreen<InfinityCellConfiguratorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private static final int CRAFT_BUTTON_X = 93;
    private static final int CRAFT_BUTTON_Y = 58;
    private static final int CRAFT_BUTTON_W = 60;
    private static final int CRAFT_BUTTON_H = 18;

    public InfinityCellConfiguratorScreen(InfinityCellConfiguratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF373737);
        graphics.renderOutline(leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xFFFFFFFF);

        drawSlot(graphics, 44, 35);
        drawSlot(graphics, 116, 35);

        // 原版熔炉箭头材质
        graphics.blit(TEXTURE, leftPos + 70, topPos + 37, 79, 35, 24, 16);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, 8 + col * 18, 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, 8 + col * 18, 142);
        }

        // 制造按钮
        int bx = leftPos + CRAFT_BUTTON_X;
        int by = topPos + CRAFT_BUTTON_Y;
        graphics.fill(bx, by, bx + CRAFT_BUTTON_W, by + CRAFT_BUTTON_H, 0xFF2E7D32);
        graphics.renderOutline(bx, by, CRAFT_BUTTON_W, CRAFT_BUTTON_H, 0xFFFFFFFF);
        graphics.drawString(font, "制造", bx + 20, by + 5, 0xFFFFFFFF, false);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(TEXTURE, leftPos + x - 1, topPos + y - 1, 55, 16, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, "样品", 44, 20, 0x404040, false);
        graphics.drawString(font, "产物", 116, 20, 0x404040, false);
        graphics.drawString(font, "物品栏", 8, 72, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int bx = leftPos + CRAFT_BUTTON_X;
            int by = topPos + CRAFT_BUTTON_Y;
            if (mouseX >= bx && mouseX <= bx + CRAFT_BUTTON_W && mouseY >= by && mouseY <= by + CRAFT_BUTTON_H) {
                ModNetwork.CHANNEL.sendToServer(new ModNetwork.ConfiguratorCraftPacket());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
