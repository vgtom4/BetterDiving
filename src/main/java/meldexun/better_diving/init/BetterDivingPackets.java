package meldexun.better_diving.init;

import java.util.function.Supplier;

import meldexun.better_diving.BetterDiving;
import meldexun.better_diving.network.packet.IPacket;
import meldexun.better_diving.network.packet.server.SPacketSyncOxygen;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.simple.SimpleChannel.MessageBuilder;

public class BetterDivingPackets {

	private static int id = 1;

	public static void registerPackets() {
		registerPacket(SPacketSyncOxygen.class, NetworkDirection.PLAY_TO_CLIENT, SPacketSyncOxygen::new);
	}

	private static <T extends IPacket> void registerPacket(Class<T> packetClass, NetworkDirection direction, Supplier<T> packetSupplier) {
		MessageBuilder<T> builder = BetterDiving.NETWORK.messageBuilder(packetClass, id++, direction);
		builder.encoder((packet, buffer) -> packet.encode(buffer));
		builder.decoder(buffer -> {
			T packet = packetSupplier.get();
			packet.decode(buffer);
			return packet;
		});
		builder.consumer((packet, ctxSupplier) -> {
			return packet.handle(ctxSupplier);
		});
		builder.add();
	}

}
