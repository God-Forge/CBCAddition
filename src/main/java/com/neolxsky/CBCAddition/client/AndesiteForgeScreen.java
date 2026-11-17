package com.neolxsky.CBCAddition.client;

import com.neolxsky.CBCAddition.block.AndesiteForgeBlock;
import com.neolxsky.CBCAddition.block.AndesiteForgeBlockEntity;
import com.neolxsky.CBCAddition.block.IOConfigurable;
import com.neolxsky.CBCAddition.menu.AndesiteForgeMenu;
import com.neolxsky.CBCAddition.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 安山合金炉 GUI：熔炉风格背景 + 六个 IO 方向按钮。
 */
public class AndesiteForgeScreen extends AbstractContainerScreen<AndesiteForgeMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private static final int BUTTON_SIZE = 16;
    private static final int IO_BUTTON_X = 152;
    private static final int IO_BUTTON_Y = 5;

    /** 相对方块朝向的展开布局：上 / 左中右 / 下后。 */
    private static final int[][] LAYOUT_POS = {{1, 0}, {0, 1}, {1, 1}, {2, 1}, {1, 2}, {2, 2}};
    private static final String[] LAYOUT_LABEL = {"上", "左", "中", "右", "下", "后"};
    private static final int CELL = 20;
    private static final int ORIGIN_X = 7;
    private static final int ORIGIN_Y = 7;

    /** 是否展开 IO 配置面板（点 IO 按钮切换）。 */
    private boolean showIO = false;

    public AndesiteForgeScreen(AndesiteForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // IO 按钮常驻显示
        int ioX = leftPos + IO_BUTTON_X;
        int ioY = topPos + IO_BUTTON_Y;
        int ioColor = showIO ? 0xFF2E7D32 : 0xFF555555;
        graphics.fill(ioX, ioY, ioX + BUTTON_SIZE, ioY + BUTTON_SIZE, ioColor);
        graphics.renderOutline(ioX, ioY, BUTTON_SIZE, BUTTON_SIZE, 0xFFFFFFFF);
        graphics.drawString(font, "IO", ioX + 2, ioY + 4, 0xFFFFFFFF, false);

        if (!showIO) {
            return;
        }

        AndesiteForgeBlockEntity be = menu.getBlockEntity();
        if (be == null) {
            return;
        }

        Direction facing = be.getBlockState().getValue(AndesiteForgeBlock.FACING);
        Direction[] faces = {
                Direction.UP,
                facing.getCounterClockWise(),
                facing,
                facing.getClockWise(),
                Direction.DOWN,
                facing.getOpposite()
        };

        for (int i = 0; i < faces.length; i++) {
            int x = leftPos + ORIGIN_X + LAYOUT_POS[i][0] * CELL;
            int y = topPos + ORIGIN_Y + LAYOUT_POS[i][1] * CELL;
            IOConfigurable.FaceMode mode = be.getFaceMode(faces[i]);

            int color = switch (mode) {
                case DISABLED -> 0xFF555555;
                case INPUT -> 0xFF2E7D32;
                case OUTPUT -> 0xFFE65100;
                case BOTH -> 0xFF1565C0;
            };

            graphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, color);
            graphics.renderOutline(x, y, BUTTON_SIZE, BUTTON_SIZE, 0xFFFFFFFF);
            graphics.drawString(font, LAYOUT_LABEL[i], x + 2, y + 4, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // IO 按钮
            int ioX = leftPos + IO_BUTTON_X;
            int ioY = topPos + IO_BUTTON_Y;
            if (mouseX >= ioX && mouseX <= ioX + BUTTON_SIZE && mouseY >= ioY && mouseY <= ioY + BUTTON_SIZE) {
                showIO = !showIO;
                return true;
            }

            if (showIO) {
                AndesiteForgeBlockEntity be = menu.getBlockEntity();
                if (be != null) {
                    Direction facing = be.getBlockState().getValue(AndesiteForgeBlock.FACING);
                    Direction[] faces = {
                            Direction.UP,
                            facing.getCounterClockWise(),
                            facing,
                            facing.getClockWise(),
                            Direction.DOWN,
                            facing.getOpposite()
                    };
                    for (int i = 0; i < faces.length; i++) {
                        int x = leftPos + ORIGIN_X + LAYOUT_POS[i][0] * CELL;
                        int y = topPos + ORIGIN_Y + LAYOUT_POS[i][1] * CELL;
                        if (mouseX >= x && mouseX <= x + BUTTON_SIZE && mouseY >= y && mouseY <= y + BUTTON_SIZE) {
                            IOConfigurable.FaceMode mode = be.getFaceMode(faces[i]).next();
                            be.setFaceMode(faces[i], mode);
                            ModNetwork.CHANNEL.sendToServer(new ModNetwork.SetFaceModePacket(
                                    be.getBlockPos(), faces[i], mode));
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
