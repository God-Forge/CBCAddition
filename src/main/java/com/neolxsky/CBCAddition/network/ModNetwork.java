package com.neolxsky.CBCAddition.network;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.block.IOConfigurable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * 网络包：客户端点击 IO 按钮 → 服务端更新方向配置。
 *
 * <p>处理时只认 {@link IOConfigurable} 接口，因此安山合金炉与黄铜合金炉共用同一个包，
 * 不会再出现「只更新了一种机器」的漏接问题。</p>
 */
public final class ModNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(CBCAddition.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, SetFaceModePacket.class,
                SetFaceModePacket::encode,
                SetFaceModePacket::decode,
                SetFaceModePacket::handle);
    }

    public record SetFaceModePacket(BlockPos pos, int face, int mode) {

        public SetFaceModePacket(BlockPos pos, Direction face, IOConfigurable.FaceMode mode) {
            this(pos, face.get3DDataValue(), mode.ordinal());
        }

        public static void encode(SetFaceModePacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos());
            buf.writeByte(packet.face());
            buf.writeByte(packet.mode());
        }

        public static SetFaceModePacket decode(FriendlyByteBuf buf) {
            return new SetFaceModePacket(buf.readBlockPos(), buf.readByte(), buf.readByte());
        }

        public static void handle(SetFaceModePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player == null) {
                    return;
                }
                BlockEntity be = player.level().getBlockEntity(packet.pos());
                if (be instanceof IOConfigurable configurable) {
                    Direction face = Direction.from3DDataValue(packet.face());
                    configurable.setFaceMode(face, IOConfigurable.FaceMode.byOrdinal(packet.mode()));
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
