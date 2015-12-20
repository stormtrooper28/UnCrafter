package github.stormtrooper28.unCrafter.Actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import github.stormtrooper28.unCrafter.UnCrafter;

public class UnDo {

	public static Boolean unCraftSmelt(ItemStack item, String name, Player p, SlotType sType, Inventory clickInv,
			Inventory inv, Inventory getInv) {

		// for(Enchantment ench : item.getEnchantments().keySet())
		// ench;

		if (sType.equals(SlotType.RESULT) && item.getType() == Material.POTION) {
			p.sendMessage(UnCrafter.prefix + ChatColor.BLUE + UnCrafter.unBrewing_is_unsupported);
			return null;
		}
		if (sType.equals(SlotType.RESULT) && item.getType() == Material.ENCHANTED_BOOK) {
			p.sendMessage(UnCrafter.prefix + ChatColor.BLUE + UnCrafter.unEnchanting_is_unsupported);
			return null;
		}
		if (sType.equals(SlotType.RESULT) && clickInv.getType() == InventoryType.MERCHANT) {
			p.sendMessage(UnCrafter.prefix + ChatColor.RED + UnCrafter.unTrading_is_unsupported);
			return null;
		}

		if (item.getType() == Material.BOW || item.getType() == Material.SHEARS
				|| item.getType() == Material.FLINT_AND_STEEL

		|| item.getType() == Material.WOOD_AXE || item.getType() == Material.WOOD_HOE
				|| item.getType() == Material.WOOD_PICKAXE || item.getType() == Material.WOOD_SPADE
				|| item.getType() == Material.WOOD_SWORD || item.getType() == Material.LEATHER_BOOTS
				|| item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_HELMET
				|| item.getType() == Material.LEATHER_LEGGINGS

		|| item.getType() == Material.STONE_AXE || item.getType() == Material.STONE_HOE
				|| item.getType() == Material.STONE_PICKAXE || item.getType() == Material.STONE_SPADE
				|| item.getType() == Material.STONE_SWORD

		|| item.getType() == Material.GOLD_AXE || item.getType() == Material.GOLD_BOOTS
				|| item.getType() == Material.GOLD_CHESTPLATE || item.getType() == Material.GOLD_HELMET
				|| item.getType() == Material.GOLD_HOE || item.getType() == Material.GOLD_LEGGINGS
				|| item.getType() == Material.GOLD_PICKAXE || item.getType() == Material.GOLD_SPADE
				|| item.getType() == Material.GOLD_SWORD

		|| item.getType() == Material.IRON_AXE || item.getType() == Material.IRON_BOOTS
				|| item.getType() == Material.IRON_CHESTPLATE || item.getType() == Material.IRON_HELMET
				|| item.getType() == Material.IRON_HOE || item.getType() == Material.IRON_LEGGINGS
				|| item.getType() == Material.IRON_PICKAXE || item.getType() == Material.IRON_SPADE
				|| item.getType() == Material.IRON_SWORD

		|| item.getType() == Material.CHAINMAIL_BOOTS || item.getType() == Material.CHAINMAIL_CHESTPLATE
				|| item.getType() == Material.CHAINMAIL_HELMET || item.getType() == Material.CHAINMAIL_LEGGINGS

		|| item.getType() == Material.DIAMOND_AXE || item.getType() == Material.DIAMOND_BOOTS
				|| item.getType() == Material.DIAMOND_CHESTPLATE || item.getType() == Material.DIAMOND_HELMET
				|| item.getType() == Material.DIAMOND_HOE || item.getType() == Material.DIAMOND_LEGGINGS
				|| item.getType() == Material.DIAMOND_PICKAXE || item.getType() == Material.DIAMOND_SPADE
				|| item.getType() == Material.DIAMOND_SWORD) {
			UnCrafter.unToolingCount++;
			return onToolUnCraftingClick(item, name, p, getInv);
		}

		if (!item.getEnchantments().isEmpty())
			p.sendMessage(UnCrafter.prefix + "Your item had" + ((item.getEnchantments().size() < 1) ? "" : "an")
					+ " enchantment" + ((item.getEnchantments().size() < 1) ? "s" : "") + "! Th"
					+ ((item.getEnchantments().size() < 1) ? "ese" : "is") + " ha"
					+ ((item.getEnchantments().size() < 1) ? "ve" : "s")
					+ " been lost due to this version of UnCrafter!");

		List<Recipe> recipes = Bukkit.getServer().getRecipesFor(item);

		if (recipes.isEmpty())
			return null;

		char invType = 'w';// First letter of inventory type

		if (getInv.getType() == InventoryType.WORKBENCH) {
			invType = 'w';
			UnCrafter.unCraftingCount++;
		}

		if (getInv.getType() == InventoryType.FURNACE) {
			invType = 'f';
			UnCrafter.unSmeltingCount++;
		}

		Random rand = new Random();

		int c = rand.nextInt(recipes.size());

		Recipe recipe = recipes.get(c);

		int leftover = item.getAmount() - recipe.getResult().getAmount();

		List<ItemStack> recip = new ArrayList<>();

		if (recipe instanceof ShapedRecipe && !UnCrafter.allow_uncrafting && invType == 'w')
			for (ItemStack i : ((ShapedRecipe) recipe).getIngredientMap().values())
				if (i != null)
					recip.add(i);
		if (recipe instanceof ShapelessRecipe && !UnCrafter.allow_uncrafting && invType == 'w')
			for (ItemStack i : ((ShapelessRecipe) recipe).getIngredientList())
				if (i != null)
					recip.add(i);
		if (recipe instanceof FurnaceRecipe && !UnCrafter.allow_unsmelting && invType == 'f')
			recip.add(((FurnaceRecipe) recipe).getInput());

		if (recip.isEmpty())
			return null;

		if (leftover >= 0) {
			int count = 0;

			for (ItemStack i : recip) {

				if (UnCrafter.isBlacklisted(i))
					return null;

				if (UnCrafter.wRand()) {
					count++;
					if ((inv.firstEmpty() < 0))
						p.getWorld().dropItem(p.getLocation(), i);
					else
						inv.addItem(i);
				}
			}
			if (count == 0)
				switch (invType) {
				default:
					break;
				case 'w':
					p.sendMessage(UnCrafter.uncrafting_item_lost + item.getType().name().toLowerCase());
					break;
				case 'f':
					p.sendMessage(UnCrafter.unsmelting_item_lost + item.getType().name().toLowerCase());
					break;
				}
			p.setItemOnCursor(new ItemStack((leftover > 0) ? (item.getType()) : (Material.AIR),
					((leftover > 0) ? leftover : 1), item.getDurability()));
		} else
			p.sendMessage((recipe instanceof FurnaceRecipe) ? UnCrafter.not_enough_items_for_unsmelting
					: UnCrafter.not_enough_items_for_uncrafting);

		return true;
	}

	private static Boolean onToolUnCraftingClick(ItemStack itemInQ, String name, Player p, Inventory inv) {
		if (!UnCrafter.allow_uncrafting)
			return null;
		if (UnCrafter.usePerm && !((Player) p).hasPermission("uncrafter.crafting"))
			return null;

		List<ItemStack> finalItems = new ArrayList<>();

		ShapedRecipe recipe = (ShapedRecipe) Bukkit.getServer().getRecipesFor(new ItemStack(itemInQ.getType())).get(0);
		Map<Character, ItemStack> ingreMap = recipe.getIngredientMap();

		int stickCount = 0;
		int mainMatCount = 0;

		Material mainMat = Material.AIR;

		for (Character c : ingreMap.keySet()) {
			if (ingreMap.get(c) != null) {
				if (ingreMap.get(c).getType().equals(Material.STICK))
					stickCount++;
				else {
					mainMatCount++;
					mainMat = ingreMap.get(c).getType();
				}
			}
		}
		if (stickCount > 0)
			for (int c = 0; c < stickCount; c++)
				if (UnCrafter.wRand())
					finalItems.add(new ItemStack(Material.STICK));

		int max = itemInQ.getType().getMaxDurability();
		int data = itemInQ.getDurability();

		int mainMatAmount = (int) Math
				.floor(Math.abs((float) mainMatCount * ((((float) max - (float) data) / (float) max))));

		if (mainMatAmount > 0)
			for (int c = 0; c < mainMatAmount; c++)
				finalItems.add(new ItemStack(mainMat));

		for (ItemStack item : finalItems) {

			if (UnCrafter.isBlacklisted(item, name))
				return null;

			if ((inv.firstEmpty() < 0))
				p.getWorld().dropItem(p.getLocation(), item);
			else
				inv.addItem(item);
		}
		p.setItemOnCursor(new ItemStack(itemInQ.getType(), itemInQ.getAmount() - 1, itemInQ.getDurability()));

		p.updateInventory();
		return true;
	}

}
