package github.stormtrooper28.unCrafter.Packets;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import github.stormtrooper28.unCrafter.UnCrafter;
import github.stormtrooper28.unCrafter.Packets.Reflection.FieldAccessor;
import github.stormtrooper28.unCrafter.Packets.Reflection.MethodInvoker;
import io.netty.channel.Channel;

public class RegisterDefaultPackets {
	
		public TinyProtocol protocol;
	
	// Chat packets
		private FieldAccessor<String> Call_Chat = Reflection.getField("{nms}.PacketPlayInChat", String.class, 0);

		// My Window packet
		private Class<?> windowClass = Reflection.getClass("{nms}.PacketPlayOutOpenWindow");
		private FieldAccessor<Byte> windowID = Reflection.getField(windowClass, Byte.class, 0);
		private FieldAccessor<Float> windowType = Reflection.getField(windowClass, float.class, 0);
		private FieldAccessor<String> windowName = Reflection.getField(windowClass, String.class, 0);
		private FieldAccessor<Byte> windowSlotCount = Reflection.getField(windowClass, Byte.class, 0);;

		// General WindowOut packet
		private Class<?> eventWindowClass = Reflection.getClass("{nms}.PacketPlayOutOpenWindow");
		private FieldAccessor<Byte> eventWindowID = Reflection.getField(windowClass, Byte.class, 0);
		private Byte windowCount = 0;

		// net.minecraft.server.ItemStack
		private Class<Object> nmsStackClass = Reflection.getUntypedClass("{nms}.ItemStack");

		// General WindowClick
		private Class<?> invClickPacket = Reflection.getClass("{nms}.PacketPlayInWindowClick");

		// Convert an NMS item stack to a CraftBukkit item stack
		private MethodInvoker getItemStack = Reflection.getMethod("{obc}.inventory.CraftItemStack", "asCraftMirror",
				nmsStackClass);

		// 0 is the player inventory
		private FieldAccessor<Integer> invClickWindowId = Reflection.getField(invClickPacket, int.class, 0);
		private FieldAccessor<Integer> invClickSlotIndex = Reflection.getField(invClickPacket, int.class, 1);
		private FieldAccessor<Integer> invClickButton = Reflection.getField(invClickPacket, int.class, 2);
		private FieldAccessor<Short> invClickActionNumber = Reflection.getField(invClickPacket, short.class, 0);
		private FieldAccessor<Object> invClickItemStack = Reflection.getField(invClickPacket, nmsStackClass, 0);
		private FieldAccessor<Integer> invClickClickMode = Reflection.getField(invClickPacket, int.class, 3);

		private static HashMap<Byte, Player> invIdtoPlayer = new HashMap<>();

		// General WindowClick
		private Class<?> invClosePacket = Reflection.getClass("{nms}.PacketPlayInCloseWindow");

		// 0 is the player inventory
		private FieldAccessor<Integer> invCloseWindowId = Reflection.getField(invClosePacket, int.class, 0);
	
	public TinyProtocol onEnable(TinyProtocol protocol, final UnCrafter uc){
		protocol = new TinyProtocol(uc) {

			@Override
			public Object onPacketInAsync(final Player sender, Channel channel, final Object packet) {
				// Cancel chat packets
				if (Call_Chat.hasField(packet)) {
					if (Call_Chat.get(packet).contains("dirty")) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override
							public void run() {
								openEnchantWindow(sender);

							}
						});
						return null;
					}
				}

				if (windowName.hasField(packet)) {
					System.out.println("Sending window field:" + packet);
				}

				if (invClickWindowId.hasField(packet) && invIdtoPlayer.containsKey(invClickWindowId.get(packet))) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						@Override
						public void run() {
							onInvClickPacket(invClickActionNumber.get(packet), invClickButton.get(packet),
									invClickClickMode.get(packet),
									(ItemStack) getItemStack.invoke(null, invClickItemStack.get(packet)),
									invClickSlotIndex.get(packet), invClickWindowId.get(packet));

						}
					});

					return null;

				}

				return super.onPacketInAsync(sender, channel, packet);
			}

			public Object onPacketOutAsync(Player reciever, Channel channel, Object packet) {
				if (eventWindowClass.isInstance(packet)) {
					Object winID = eventWindowID.get(packet);

					windowCount = (Byte) winID;

					invIdtoPlayer.put(windowCount, reciever);

					// Which is equivalent to:
					// serverPing.get(packet).setPlayerSample(new
					// ServerPingPlayerSample(1000, 0));
					return packet;
				}

				if (invCloseWindowId.hasField(packet))
					invIdtoPlayer.remove(invCloseWindowId.get(packet));

				return super.onPacketOutAsync(reciever, channel, packet);
			}

		};
		for (Player preP : Bukkit.getServer().getOnlinePlayers())
			protocol.injectPlayer(preP);
		this.protocol = protocol;
		return protocol;
	}
	


	// invClickActionNumber, invClickButton, invClickClickMode,
	// invClickItemStack, invClickSlotIndex, invClickWindowId

	public void onInvClickPacket(Short actionNumber, Integer button, Integer clickMode, Object itemStack,
			Integer slotIndex, Integer windowId) {
		// TODO Auto-generated method stub
		System.out.println("Cancelled Incoming Click");
	}

	public void openEnchantWindow(Player player) {
		System.out.println("ddiirrttyy");

		try {
			// Only visible for the client
			Object windowPacket = windowClass.newInstance();

			windowName.set(windowPacket, "UnEnchanter");
			windowID.set(windowPacket, 255);
			windowSlotCount.set(windowPacket, 37);
			windowType.set(windowPacket, "4");

			// Send the packet to the player
			protocol.sendPacket(player, windowPacket);
		} catch (Exception e) {
			throw new RuntimeException("Cannot send packet.", e);
		}

	}
	
}
