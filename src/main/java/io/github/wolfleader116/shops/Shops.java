package io.github.wolfleader116.shops;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import io.github.wolfleader116.shops.commands.ShopsC;
import io.github.wolfleader116.wolfapi.ItemModifiers;
import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Shops extends JavaPlugin implements Listener {
	private static final Logger log = Logger.getLogger("Minecraft");

	public static Economy economy = null;

	public static Shops plugin;

	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		if (Bukkit.getServer().getPluginManager().getPlugin("WolfAPI") == null) {
			log.severe("WolfAPI was not found on the server! Disabling Shops!");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
		setupEconomy();
		getCommand("shops").setExecutor(new ShopsC());
		Bukkit.getPluginManager().registerEvents(this, this);
		plugin = this;
		if (this.getConfig().getInt("Version") != 2) {
			File conf = new File(this.getDataFolder(), "config.yml");
			conf.delete();
			this.saveDefaultConfig();
			this.saveConfig();
			this.reloadConfig();
		}
	}

	@Override
	public void onDisable() {
		plugin = null;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent e) {
		Player p = e.getPlayer();
		if (e.getLine(0).toLowerCase().contains("[shop]")) {
			if (p.hasPermission("shops.create")) {
				for (String name : this.getConfig().getConfigurationSection("Items").getKeys(false)) {
					if (e.getLine(1).equalsIgnoreCase(name)) {
						e.setLine(0, "§5[§aShop§5]");
						e.setLine(1, WordUtils.capitalizeFully(name.replace("_", " ").toLowerCase()));
						e.setLine(2, "");
						e.setLine(3, "§");
					}
				}
				if (e.getLine(3) != "§") {
					p.sendMessage(ChatColor.BLUE + "Shops> " + ChatColor.GREEN + "Invalid format. Shop not created.");
					e.setLine(3, "");
				} else {
					e.setLine(3, "");
				}
			} else {
				p.sendMessage(ChatColor.BLUE + "Shops> " + ChatColor.GREEN + "You do not have permission to create a Shop sign.");
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = e.getClickedBlock();
			if ((b.getType() == Material.SIGN_POST) || (b.getType() == Material.WALL_SIGN)) {
				Sign s = (Sign)b.getState();
				if (s.getLine(0).equals("§5[§aShop§5]")) {
					if (p.hasPermission("shops.use")) {
						for (String name : this.getConfig().getConfigurationSection("Items").getKeys(false)) {
							if (s.getLine(1).equalsIgnoreCase(name)) {
								Inventory inv = createInventory((short) 45, "§f§9" + WordUtils.capitalizeFully(name.replace("_", " ").toLowerCase()) + " Shop");
								int number = 0;
								Set<String> itemlist = this.getConfig().getConfigurationSection("Items." + name).getKeys(false);
								try {
									for (String itemsdata : itemlist) {
										String[] itemdata = itemsdata.split("'");
										String[] maindata = itemdata[0].split(",");
										String matname = maindata[0];
										Material material = Material.getMaterial(matname.toUpperCase());
										int amount = Integer.valueOf(maindata[1]);
										short data = Short.valueOf(maindata[2]);
										String price = maindata[3];
										boolean isSoulbound = Boolean.valueOf(maindata[4]);
										boolean isFinal = Boolean.valueOf(maindata[5]);
										boolean isUnbreakable = Boolean.valueOf(maindata[6]);
										List<String> lore;
										if (this.getConfig().contains("Items." + name + "." + itemsdata)) {
											lore = this.getConfig().getStringList("Items." + name + "." + itemsdata);
										} else {
											lore = new ArrayList<String>();
										}
										try {
											ArrayList<String> enchantments = new ArrayList<String>();
											for(int i = 1; i < itemdata.length; i++) {
												String enchdata = itemdata[i];
												enchantments.add(enchdata);
											}
											inv.setItem(number, createEnchantedItem(material, amount, data, price, enchantments, isSoulbound, isFinal, isUnbreakable, lore));
										} catch (ArrayIndexOutOfBoundsException ex) {
											inv.setItem(number, createItem(material, amount, data, price, isSoulbound, isFinal, isUnbreakable, lore));
										}
										number++;
									}
								} catch (NullPointerException exc) {
									p.sendMessage(ChatColor.BLUE + "Shops> " + ChatColor.GREEN + "There is an error in the config! Please notify an admin.");
								}
								p.openInventory(inv);
							}
						}
					} else {
						p.sendMessage(ChatColor.BLUE + "Shops> " + ChatColor.GREEN + "You do not have permission to use a Shop sign.");
					}
				}
			}
		}
	}

	public Inventory createInventory(short slots, String name) {
		Inventory invent = Bukkit.createInventory(null, slots, name);
		return invent;
	}

	public ItemStack createEnchantedItem(Material material, int amount, short data, String price, ArrayList<String> enchantments, boolean isSoulbound, boolean isFinal, boolean isUnbreakable, List<String> lore) {
		ItemStack item = new ItemStack(material, amount, data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + WordUtils.capitalizeFully(item.getType().name().replace("_", " ").toLowerCase()));
		if (lore != null) {
			lore.add("");
			lore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + price);
			lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + Integer.toString(amount));
		} else {
			lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + price);
			lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + Integer.toString(amount));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		for (String enchinfo : enchantments) {
			String[] enchdata = enchinfo.split(",");
			int level = Integer.valueOf(enchdata[1]);
			Enchantment ench = Enchantment.getByName(enchdata[0].toUpperCase());
			item.addUnsafeEnchantment(ench, level);
		}
		if (isSoulbound) {
			item = ItemModifiers.setSoulbound(item);
		}
		if (isFinal) {
			item = ItemModifiers.setFinal(item);
		}
		if (isUnbreakable) {
			item = ItemModifiers.setUnbreakable(item);
		}
		return item;
	}

	public ItemStack createItem(Material material, int amount, short data, String price, boolean isSoulbound, boolean isFinal, boolean isUnbreakable, List<String> lore) {
		ItemStack item = new ItemStack(material, amount, data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + WordUtils.capitalizeFully(item.getType().name().replace("_", " ").toLowerCase()));
		if (lore != null) {
			lore.add("");
			lore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + price);
			lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + Integer.toString(amount));
		} else {
			lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + price);
			lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + Integer.toString(amount));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		if (isSoulbound) {
			item = ItemModifiers.setSoulbound(item);
		}
		if (isFinal) {
			item = ItemModifiers.setFinal(item);
		}
		if (isUnbreakable) {
			item = ItemModifiers.setUnbreakable(item);
		}
		return item;
	}

	public ItemStack createBoughtItem(Material material, int amount, short data, boolean isSoulbound, boolean isFinal, boolean isUnbreakable, List<String> lore) {
		ItemStack item = new ItemStack(material, amount, data);
		if (lore != null) {
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		if (isSoulbound) {
			item = ItemModifiers.setSoulbound(item);
		}
		if (isFinal) {
			item = ItemModifiers.setFinal(item);
		}
		if (isUnbreakable) {
			item = ItemModifiers.setUnbreakable(item);
		}
		return item;
	}

	public ItemStack createEnchantedBoughtItem(Material material, int amount, short data, ArrayList<String> enchantments, boolean isSoulbound, boolean isFinal, boolean isUnbreakable, List<String> lore) {
		ItemStack item = new ItemStack(material, amount, data);
		if (lore != null) {
			ItemMeta meta = item.getItemMeta();
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		for (String enchinfo : enchantments) {
			String[] enchdata = enchinfo.split(",");
			int level = Integer.valueOf(enchdata[1]);
			Enchantment ench = Enchantment.getByName(enchdata[0].toUpperCase());
			item.addUnsafeEnchantment(ench, level);
		}
		if (isSoulbound) {
			item = ItemModifiers.setSoulbound(item);
		}
		if (isFinal) {
			item = ItemModifiers.setFinal(item);
		}
		if (isUnbreakable) {
			item = ItemModifiers.setUnbreakable(item);
		}
		return item;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			if (e.getCurrentItem().getType() != Material.AIR) {
				Player player = (Player) e.getWhoClicked();
				int clicked = e.getSlot();
				Inventory inventory = e.getClickedInventory();
				for (String name : this.getConfig().getConfigurationSection("Items").getKeys(false)) {
					if (inventory.getTitle().equalsIgnoreCase("§f§9" + WordUtils.capitalizeFully(name.replace("_", " ").toLowerCase()) + " Shop")) {
						String invname = inventory.getTitle().replace("§f§9", "");
						invname = invname.replace(" Shop", "");
						invname = invname.replace(" ", "_");
						Set<String> itemlist = this.getConfig().getConfigurationSection("Items." + invname).getKeys(false);
						String[] items = itemlist.toArray(new String[0]);
						if (items.length > clicked) {
							String itemsdata = items[clicked];
							String[] itemdata = itemsdata.split("'");
							String[] maindata = itemdata[0].split(",");
							String matname = maindata[0];
							Material material = Material.getMaterial(matname.toUpperCase());
							int amount = Integer.valueOf(maindata[1]);
							short data = Short.valueOf(maindata[2]);
							String price = maindata[3];
							boolean isSoulbound = Boolean.valueOf(maindata[4]);
							boolean isFinal = Boolean.valueOf(maindata[5]);
							boolean isUnbreakable = Boolean.valueOf(maindata[6]);
							List<String> lore;
							if (this.getConfig().contains("Items." + name + "." + itemsdata)) {
								lore = this.getConfig().getStringList("Items." + name + "." + itemsdata);
							} else {
								lore = new ArrayList<String>();
							}
							buyitem(player, price, material, amount, data, itemdata, isSoulbound, isFinal, isUnbreakable, lore);
							try {
								ArrayList<String> enchantments = new ArrayList<String>();
								for(int i = 1; i < itemdata.length; i++) {
									String enchdata = itemdata[i];
									enchantments.add(enchdata);
								}
								createEnchantedBoughtItem(material, amount, data, enchantments, isSoulbound, isFinal, isUnbreakable, lore);
							} catch (ArrayIndexOutOfBoundsException ex) {
								createBoughtItem(material, amount, data, isSoulbound, isFinal, isUnbreakable, lore);
							}
						}
					} else if (e.getInventory().getTitle().equalsIgnoreCase("§f§9" + WordUtils.capitalizeFully(name.replace("_", " ").toLowerCase()) + " Shop")) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	public void buyitem(Player player, String price, Material material, int amount, short data, String[] itemdata, boolean isSoulbound, boolean isFinal, boolean isUnbreakable, List<String> lore) {
		economy.withdrawPlayer(player, Integer.parseInt(price));
		try {
			ArrayList<String> enchantments = new ArrayList<String>();
			for(int i = 1; i < itemdata.length; i++) {
				String enchdata = itemdata[i];
				enchantments.add(enchdata);
			}
			player.getInventory().addItem(createEnchantedBoughtItem(material, amount, data, enchantments, isSoulbound, isFinal, isUnbreakable, lore));
		} catch (ArrayIndexOutOfBoundsException ex) {
			
			player.getInventory().addItem(createBoughtItem(material, amount, data, isSoulbound, isFinal, isUnbreakable, lore));
		}
		player.sendMessage(ChatColor.BLUE + "Shops> " + ChatColor.GREEN + "Purchased the item!");
		player.closeInventory();
	}
}