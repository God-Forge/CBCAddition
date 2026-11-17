package com.neolxsky.CBCAddition.block;

import net.minecraft.core.Direction;

/**
 * 六面 IO 配置的公共契约。
 *
 * <p>安山合金炉与黄铜合金炉结构完全一致，因此共用同一套 GUI 与网络包。
 * 服务端处理「切换某个面的模式」时只认这个接口，不再依赖具体方块实体类型，
 * 从而避免「客户端发了包、服务端只认一种机器」的漏接问题。</p>
 */
public interface IOConfigurable {

    /**
     * 每个面的 IO 模式。
     *
     * <p>枚举顺序即「点击循环」顺序；NBT 中按 {@link #ordinal()} 存储，
     * 追加新值请放在末尾以保持存档兼容。</p>
     */
    enum FaceMode {
        DISABLED,
        INPUT,
        OUTPUT,
        BOTH;

        /** 循环到下一个模式。 */
        public FaceMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public boolean canInput() {
            return this == INPUT || this == BOTH;
        }

        public boolean canOutput() {
            return this == OUTPUT || this == BOTH;
        }

        /** 由序号还原模式，越界时夹到合法范围（防止旧存档或脏数据崩溃）。 */
        public static FaceMode byOrdinal(int ordinal) {
            FaceMode[] modes = values();
            return modes[Math.max(0, Math.min(ordinal, modes.length - 1))];
        }
    }

    /** 读取指定面的模式。 */
    FaceMode getFaceMode(Direction direction);

    /** 写入指定面的模式（实现方负责 setChanged）。 */
    void setFaceMode(Direction direction, FaceMode mode);
}
