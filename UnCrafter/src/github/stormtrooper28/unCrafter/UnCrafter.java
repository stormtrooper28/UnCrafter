package github.stormtrooper28.unCrafter; //\UnCrafter.jar			&1 &2 &3 & 4 ...

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import github.stormtrooper28.unCrafter.Actions.OpenInv;
import github.stormtrooper28.unCrafter.Actions.UnDo;
import github.stormtrooper28.unCrafter.Metrics.Metrics;
import github.stormtrooper28.unCrafter.Metrics.Metrics.Graph;
import github.stormtrooper28.unCrafter.Packets.Reflection;
import github.stormtrooper28.unCrafter.Packets.Reflection.FieldAccessor;
import github.stormtrooper28.unCrafter.Packets.Reflection.MethodInvoker;
import github.stormtrooper28.unCrafter.Packets.TinyProtocol;
import github.stormtrooper28.unCrafter.Utils.AutoUpdater;
import io.netty.channel.Channel;

public class UnCrafter extends JavaPlugin implements Listener {
	// Messages Config
	// * * * * * * * * * * * * * * * * * * * *

	private String default_update_news;
	private String no_updates_available;
	private String blacklist_not_found;
	private String world_include_list_invalid;
	private String world_exclude_list_invalid;
	private String checking_updates;
	private String update_check_failed;
	private String update_check_complete;
	public static String unBrewing_is_unsupported;
	public static String unEnchanting_is_unsupported;
	public static String unTrading_is_unsupported;
	public static String not_enough_items_for_uncrafting;
	public static String uncrafting_item_lost;
	public static String unsmelting_item_lost;
	public static String not_enough_items_for_unsmelting;
	// private String chat_clear_prefix;
	// private String chat_clear_suffix;

	// * * * * * * * * * * * * * * * * * * * *

	private static String msgYML;

	public UnCrafter plugin = this;
	public Plugin plu = this;
	public UnCrafter uc = this;
	private static boolean pass = true;
	// private static boolean isReloading = false;
	public static boolean usePerm = false;
	private static boolean sendMsg = false;
	public static boolean allow_uncrafting = true;
	public static boolean allow_unsmelting = true;
	private static String wobw = "none"; // WhiteOrBlackWorlds
	private static float recycleChance = 100.0f;
	public static float scv = 0.0f; // serverConfigVersion
	private static float pcv = 0.0f; // pluginConfigVersion
	private static List<String> recycleBlacklist = new ArrayList<>();
	private static List<String> whiteWorlds = new ArrayList<>();
	private static List<String> blackWorlds = new ArrayList<>();
	public boolean updateAvailable = false;
	public String updateNews = default_update_news;
	public String downloadUrl = "http://pastebin.com/raw.php?i=e72zBYhb";
	@SuppressWarnings("unused")
	private static AutoUpdater au;
	public String updateMessage = no_updates_available;
	public static int unCraftingCount = 0;
	public static int unSmeltingCount = 0;
	public static int unToolingCount;
	public final static String prefix = ChatColor.BLUE + "[" + ChatColor.LIGHT_PURPLE + "UnCrafter" + ChatColor.BLUE
			+ "] " + ChatColor.DARK_AQUA;

	// w: workbench
	// f: furnace
	// e: enchant
	// b: brewing stand
	public static List<Block> unBlockList = new ArrayList<>();

	public static List<Entity> villagerList = new ArrayList<>();

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

	private TinyProtocol protocol;

	@Override
	public void onEnable() {

		if (getConfig().getDouble("config_version") <= 1.9) // Update
															// Messages
			// and delete
			// SaveData
			try {
				Files.delete(Paths.get(getDataFolder().getAbsolutePath(), "SaveData.yml"));
				Files.copy(Paths.get(getDataFolder().getAbsolutePath(), msgYML),
						Paths.get(getDataFolder().getAbsolutePath(), "OldMessages.yml"));
				if (Files.deleteIfExists(Paths.get(getDataFolder().getAbsolutePath(), msgYML)))
					getLogger()
							.severe("Messages.yml was reset for version upgrade, update your Messages from OldMessages.yml!"
									+ "Any messages not in the new Messages.yml are no longer used!");
				getConfig().set("config_version", 1.9);
			} catch (Exception e) {
			}

		getServer().getPluginManager().registerEvents(this, this);

		getMessages().options().copyDefaults(true);
		saveDefaultMessages();

		saveDefaultConfig();
		getConfig().options().copyHeader(true);
		getConfig().options().copyDefaults(true);
		saveConfig();

		iniMessages();

		String pluginVersion = plugin.getDescription().getVersion();

		List<Character> chars = new ArrayList<>();

		// for(int c = 0; c < 10; c++)
		// chars.add((char) c);
		chars.add('0');
		chars.add('1');
		chars.add('2');
		chars.add('3');
		chars.add('4');
		chars.add('5');
		chars.add('6');
		chars.add('7');
		chars.add('8');
		chars.add('9');
		chars.add('#');

		pluginVersion = pluginVersion.replaceFirst(".", "#");

		for (int c = 0; c < pluginVersion.length(); c++) {
			if (!chars.contains(pluginVersion.charAt(c)))
				pluginVersion = pluginVersion.replace(pluginVersion.charAt(c), ' ');
		}
		pluginVersion = pluginVersion.replaceAll(" ", "");

		pluginVersion = pluginVersion.replaceFirst("#", ".");

		scv = Float.valueOf(pluginVersion);

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				reloadMessages();
				reloadConfig();

				String preLang = "en";

				if (getConfig().get("allow_uncrafting") != null)
					preLang = getConfig().getString("language");
				else
					getConfig().set("language", "EN");

				if (preLang.toLowerCase().startsWith("s") || preLang.toLowerCase().startsWith("es"))
					msgYML = "ESMessages.yml";
				else if (preLang.toLowerCase().startsWith("g") || preLang.toLowerCase().startsWith("d"))
					msgYML = "DEMessages.yml";
				else if (preLang.toLowerCase().startsWith("f"))
					msgYML = "FRMessages.yml";
				else
					msgYML = "Messages.yml";
				getLogger().info("Using " + msgYML);

				/* 1 */if (getConfig().get("allow_uncrafting") != null)
					UnCrafter.allow_uncrafting = getConfig().getBoolean("allow_uncrafting");
				else
					getConfig().set("allow_uncrafting", false);

				/* 2 */if (getConfig().get("allow_unsmelting") != null)
					UnCrafter.allow_unsmelting = getConfig().getBoolean("allow_unsmelting");
				else
					getConfig().set("allow_unsmelting", false);

				/* 3 */if (getConfig().get("use_permissions") != null)
					UnCrafter.usePerm = getConfig().getBoolean("use_permissions");
				else
					getConfig().set("use_permissions", false);

				/* 4 */if (getConfig().get("notify_update_available") != null)
					UnCrafter.sendMsg = getConfig().getBoolean("notify_update_available");
				else
					getConfig().set("notify_update_available", true);

				/*
				 * if (getConfig().get("send_auto_update_option") != null)
				 * UnCrafter.sendOption = getConfig().getBoolean(
				 * "send_auto_update_option"); else
				 * getConfig().set("send_auto_update_option", false);
				 */

				/* 5 */if (getConfig().get("recycle_chance") != null) {
					UnCrafter.recycleChance = (float) getConfig().getDouble("recycle_chance") * 100;
				} else
					getConfig().set("recycle_chance", 1.0);

				/* 6 */if (getConfig().get("config_version") != null) {
					UnCrafter.scv = (float) getConfig().getDouble("config_version");
				} else
					getConfig().set("config_version", pcv);

				/* 7 */if (getConfig().get("recycle_blacklist") != null) {
					UnCrafter.recycleBlacklist = getConfig().getStringList("recycle_blacklist");
				} else {
					getLogger().warning(blacklist_not_found);
					List<String> blacklist = new ArrayList<>();
					blacklist.add("coal_ore");
					blacklist.add("quartz_ore");
					blacklist.add("redstone_ore");
					blacklist.add("lapis_ore");
					getConfig().set("recycle_blacklist", blacklist);
					UnCrafter.recycleBlacklist = blacklist;
				}
				/* 8 */if (!UnCrafter.recycleBlacklist.isEmpty())
					for (String s : UnCrafter.recycleBlacklist)
						UnCrafter.recycleBlacklist.set(UnCrafter.recycleBlacklist.indexOf(s), s.toLowerCase());

				/* 9 */if (getConfig().get("white_Or_Black_list_Worlds") != null)
					UnCrafter.wobw = getConfig().getString("white_Or_Black_list_Worlds");
				else
					getConfig().set("white_Or_Black_list_Worlds", "none");

				wobw = wobw.toLowerCase();

				if (wobw.startsWith("in"))
					wobw = "white";
				if (wobw.startsWith("ex"))
					wobw = "black";

				/* 10 */if (getConfig().get("worlds_whitelist") != null)
					UnCrafter.whiteWorlds = getConfig().getStringList("worlds_whitelist");
				else {
					if (wobw == "white") {
						wobw = "none";
						getLogger().warning(world_include_list_invalid);
					}
				}

				/* 11 */if (getConfig().get("worlds_blacklist") != null)
					UnCrafter.whiteWorlds = getConfig().getStringList("worlds_blacklist");
				else {
					if (wobw == "black") {
						wobw = "none";
						;
						getLogger().warning(world_exclude_list_invalid);
					}
				}

				/* 12 */if (getConfig().get("unToolingCount") != null)
					UnCrafter.unToolingCount = getConfig().getInt("unToolingCount");
				else
					getConfig().set("unToolingCount", unToolingCount);

				/* 13 */if (getConfig().get("unCraftingCount") != null)
					UnCrafter.unCraftingCount = getConfig().getInt("unCraftingCount");
				else
					getConfig().set("unCraftingCount", unCraftingCount);

				/* 14 */if (getConfig().get("unSmeltingCount") != null)
					UnCrafter.unSmeltingCount = getConfig().getInt("unSmeltingCount");
				else
					getConfig().set("unSmeltingCount", unSmeltingCount);

				saveConfig();

			}

		}, 10);

		pass = true;
		try {
			UnCrafter.au = new AutoUpdater(this);
			getLogger().info(checking_updates);
		} catch (IOException e) {
			getLogger().warning(update_check_failed);
			pass = false;
		}

		if (pass)
			getLogger().info(update_check_complete);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				if (pass)
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (updateAvailable) {
							getLogger().info(ChatColor.stripColor(updateMessage + "\n" + updateNews));
							if (!usePerm && p.isOp()) {
								if (sendMsg)
									p.sendMessage(updateMessage + "\n" + updateNews);

							} else if (usePerm) {
								if (sendMsg && p.hasPermission("uncrafter.update.notify"))
									p.sendMessage(updateMessage + "\n" + updateNews);
							}
						}
					}

			}

		}, 60, 432000); // 432000 Is 6 hours

		pass = true;

		try {
			Metrics metrics = new Metrics(plugin);

			Graph weaponsUsedGraph = metrics.createGraph("Type used");

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("UnCrafting") {

				@Override
				public int getValue() {
					return unCraftingCount; // Number of players who used
											// UnCrafting
				}

			});

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("UnTooling") {

				@Override
				public int getValue() {
					return unToolingCount; // Number of players who used
											// UnTooling
				}

			});

			weaponsUsedGraph.addPlotter(new Metrics.Plotter("UnSmelting") {

				@Override
				public int getValue() {
					return unSmeltingCount;
				}

			});

			metrics.start();
		} catch (IOException e) {
		}

		protocol = new TinyProtocol(this) {

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
	}

	@EventHandler
	public void playerInjecting(PlayerJoinEvent e) {
		protocol.injectPlayer(e.getPlayer());
	}

	// invClickActionNumber, invClickButton, invClickClickMode,
	// invClickItemStack, invClickSlotIndex, invClickWindowId

	protected void onInvClickPacket(Short actionNumber, Integer button, Integer clickMode, Object itemStack,
			Integer slotIndex, Integer windowId) {
		// TODO Auto-generated method stub
		System.out.println("Cancelled Incoming Click");
	}

	protected void openEnchantWindow(Player player) {
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

	@Override
	public void onDisable() {
		getConfig().set("unSmeltingCount", unSmeltingCount);
		getConfig().set("unToolingCount", unToolingCount);
		getConfig().set("unCraftingCount", unCraftingCount);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (sender instanceof Player && cmd.toString().equalsIgnoreCase("uncraft")) {
			if ((usePerm && sender.hasPermission("uncrafter.command")) || sender.isOp()) {
				Player p = (Player) sender;
				ItemStack item = p.getItemInHand();
				short dura = item.getDurability();
				String name = item.getType().toString();
				if (dura != 0)
					name = name + "#" + dura;
				if (UnCrafter.isBlacklisted(item, name))
					return false;
				SlotType sType = SlotType.QUICKBAR;
				Inventory inv = p.getInventory();
				p.openInventory(inv);
				UnDo.unCraftSmelt(item, name, p, sType, inv, inv, inv);
				p.closeInventory();
			}
			return true;
		}

		return false;
	}

	@EventHandler
	public void onNoSaveClick(InventoryClickEvent e) {
		if (wobw.equalsIgnoreCase("black")) {
			if (isBlacklistedWorld(e.getWhoClicked().getWorld()))
				return;
		} else if (wobw.equalsIgnoreCase("white"))
			if (!isWhitelistedWorld(e.getWhoClicked().getWorld()))
				return;

		if (!e.getSlotType().equals(SlotType.RESULT))
			return;

		if (e.getView().getTopInventory().getItem(0) != null)
			return;

		ItemStack item;
		if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR)
			item = e.getCurrentItem();
		else if (e.getCursor() != null && e.getCursor().getType() != Material.AIR)
			item = e.getCursor();
		else
			return;
		short dura = item.getDurability();
		String name = item.getType().toString();
		if (dura != 0)
			name = name + "#" + dura;

		if (UnCrafter.isBlacklisted(item, name))
			return;
		Player p = (Player) e.getWhoClicked();

		SlotType sType = e.getSlotType();

		Inventory clickInv = e.getClickedInventory();

		Inventory inv = p.getInventory();

		Inventory getInv = e.getInventory();

		if ((UnDo.unCraftSmelt(item, name, p, sType, clickInv, inv, getInv) != null)
				? UnDo.unCraftSmelt(item, name, p, sType, clickInv, inv, getInv) : false)
			e.setCancelled(true);

	}

	// On Interact with custom table or furnace...
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() != null) {
			Block block = e.getClickedBlock();
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && unBlockList.contains(block)) {
				if (block.getType() == Material.WORKBENCH) {
					OpenInv.openUnCraftingInv(e.getPlayer());
					e.setCancelled(true);
				} else if (block.getType() == Material.FURNACE) {
					OpenInv.openUnSmeltingInv(e.getPlayer());
					e.setCancelled(true);
				} else if (block.getType() == Material.ENCHANTMENT_TABLE) {
					OpenInv.openUnEnchantingInv(e.getPlayer());
					e.setCancelled(true);
				} else if (block.getType() == Material.BREWING_STAND) {
					OpenInv.openUnBrewingInv(e.getPlayer());
					e.setCancelled(true);
				} else
					unBlockList.remove(block);
			} else
				unBlockList.remove(block);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (unBlockList.remove(e.getBlock()))
			unBlockList.remove(e.getBlock());
	}

	/*
	 * private void workbenchFill(List<ItemStack> items) {
	 * 
	 * }
	 * 
	 * private void workbenchFill(ItemStack item) {
	 * 
	 * }
	 */

	@EventHandler
	private void unTests(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if (e.getMessage().toLowerCase().contains("unenchant")) {

			InventoryView invView = p.openEnchanting(null, true);

			invView.setProperty(Property.ENCHANT_BUTTON1, 0);
		} else if (e.getMessage().toLowerCase().contains("win")) {

			// PacketPlayOutOpenWindow pack = new PacketPlayOutOpenWindow(9,
			// "Chest", ChatSerializer.a("{\"text\":\"Welcome\"}"), 9, 9);

			try {
				Object enumTitle = getNMSClass("PacketPlayOutOpenWindow").getDeclaredClasses()[0].getField("TITLE")
						.get(null);
				Object chat = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
						.invoke(null, "{\"text\":\"Welcome\"}");

				Constructor<?> titleConstructor = getNMSClass("PacketPlayOutOpenWindow").getConstructor(
						getNMSClass("PacketPlayOutOpenWindow").getDeclaredClasses()[0],
						getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
				Object packet = titleConstructor.newInstance(enumTitle, chat, 20, 40, 20);

				sendPacket(e.getPlayer(), packet);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (e.getMessage().toLowerCase().contains("unsmelt")) {

			Inventory testInv = Bukkit.createInventory(p, InventoryType.FURNACE, "CustomFurnace");

			InventoryView invView = p.openInventory(testInv);

			invView.setProperty(Property.TICKS_FOR_CURRENT_FUEL, 20000);
		}

	}

	public static Boolean wRand() { // Weighted Binary Random (yes/no)
		Random rand = new Random();
		int ran = rand.nextInt(100);
		return (ran <= recycleChance);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {

			@Override
			public void run() {
				if (updateAvailable) {
					if (!usePerm && p.isOp()) {
						if (sendMsg)
							p.sendMessage(updateMessage + "\n" + updateNews);

					} else if (usePerm) {
						if (sendMsg && p.hasPermission("uncrafter.update.notify"))
							p.sendMessage(updateMessage + "\n" + updateNews);
					}
				}
			}

		}, 60);
	}

	public static boolean isBlacklisted(ItemStack result, String name) {
		if (result == null || name == null)
			return true;

		if (recycleBlacklist.size() < 1)
			return false;
		for (String check : recycleBlacklist) {
			check = check.toUpperCase();

			if (result.getType().equals(Material.matchMaterial(check)) || name.equalsIgnoreCase(check))
				return true;
		}
		return false;

	}

	public static boolean isBlacklisted(ItemStack result) {
		if (result == null)
			return true;

		if (recycleBlacklist.size() < 1)
			return false;
		for (String check : recycleBlacklist) {
			check = check.toUpperCase();

			if (result.getType().equals(Material.matchMaterial(check)))
				return true;
		}
		return false;

	}

	private boolean isBlacklistedWorld(World world) {
		return isBlacklistedWorld(world.getName());
	}

	private boolean isBlacklistedWorld(String world) {

		for (String wor : blackWorlds)
			if (wor.equals(world))
				return true;

		return false;
	}

	private boolean isWhitelistedWorld(World world) {
		return isWhitelistedWorld(world.getName());
	}

	private boolean isWhitelistedWorld(String world) {

		for (String wor : whiteWorlds)
			if (wor.equals(world))
				return true;

		return false;
	}

	private void iniMessages() {
		reloadMessages();
		default_update_news = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("default_update_news"));
		no_updates_available = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("no_updates_available"));
		blacklist_not_found = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("blacklist_not_found"));
		world_include_list_invalid = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("world_include_list_invalid"));
		world_exclude_list_invalid = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("world_exclude_list_invalid"));
		checking_updates = ChatColor.translateAlternateColorCodes('&', getMessages().getString("checking_updates"));
		update_check_failed = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("update_check_failed"));
		update_check_complete = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("update_check_complete"));
		unBrewing_is_unsupported = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("unBrewing_is_unsupported"));
		unEnchanting_is_unsupported = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("unEnchanting_is_unsupported"));
		unTrading_is_unsupported = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("unTrading_is_unsupported"));
		not_enough_items_for_uncrafting = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("not_enough_items_for_uncrafting"));
		uncrafting_item_lost = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("uncrafting_item_lost"));
		unsmelting_item_lost = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("unsmelting_item_lost"));
		not_enough_items_for_unsmelting = ChatColor.translateAlternateColorCodes('&',
				getMessages().getString("not_enough_items_for_unsmelting"));
	}

	public void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Class<?> getNMSClass(String name) {
		// org.bukkit.craftbukkit.v1_8_R3...
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		}

		catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private FileConfiguration Messages = null;
	private File MessagesFile = null;

	public void reloadMessages() {
		if (MessagesFile == null) {
			MessagesFile = new File(getDataFolder(), msgYML);
		}
		Messages = YamlConfiguration.loadConfiguration(MessagesFile);

		Reader defConfigStream = null;
		// Look for defaults in the jar
		try {
			defConfigStream = new InputStreamReader(this.getResource(msgYML), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			Messages.setDefaults(defConfig);
		}
	}

	// read the save data
	public FileConfiguration getMessages() {
		if (Messages == null) {
			reloadMessages();
		}
		return Messages;
	}

	// save the file
	public void saveMessages() {
		if (Messages == null || MessagesFile == null) {
			return;
		}
		try {
			getMessages().save(MessagesFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save config to " + MessagesFile, ex);
		}
	}

	// implement backup with following
	public void saveDefaultMessages() {
		if (MessagesFile == null) {
			MessagesFile = new File(plugin.getDataFolder(), msgYML);
		}
		if (!MessagesFile.exists()) {
			plugin.saveResource(msgYML, false);
		}
	}

	private FileConfiguration Data = null;
	private File DataFile = null;

	public void reloadData() {
		if (DataFile == null) {
			DataFile = new File(getDataFolder(), "Data.yml");
		}
		Data = YamlConfiguration.loadConfiguration(DataFile);

		Reader defConfigStream = null;
		// Look for defaults in the jar
		try {
			defConfigStream = new InputStreamReader(this.getResource("Data.yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			Data.setDefaults(defConfig);
		}
	}

	// read the save data
	public FileConfiguration getData() {
		if (Data == null) {
			reloadData();
		}
		return Data;
	}

	// save the file
	public void saveData() {
		if (Data == null || DataFile == null) {
			return;
		}
		try {
			getData().save(DataFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save config to " + DataFile, ex);
		}
	}

	// implement backup with following
	public void saveDefaultData() {
		if (DataFile == null) {
			DataFile = new File(plugin.getDataFolder(), "Data.yml");
		}
		if (!DataFile.exists()) {
			plugin.saveResource("Data.yml", false);
		}
	}

}
