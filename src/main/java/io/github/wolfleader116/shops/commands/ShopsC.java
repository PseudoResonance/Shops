package io.github.wolfleader116.shops.commands;

import java.io.File;
import java.util.logging.Logger;

import io.github.wolfleader116.shops.Shops;
import io.github.wolfleader116.wolfapi.Errors;
import io.github.wolfleader116.wolfapi.WolfAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopsC implements CommandExecutor {

	private static final Logger log = Logger.getLogger("Minecraft");

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("shops")) {
			File configFile = new File(Shops.plugin.getDataFolder(), "config.yml");
			if (!(sender instanceof Player)) {
				if (args.length == 0) {
					log.info("Use /shops help for a list of commands.");
					log.info("Shops plugin created by WolfLeader116");
					log.info("===---Shops Info---===");
				} else if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("help")) {
						log.info("/shops reload Reloads the config.");
						log.info("/shops reset Resets the config.");
						log.info("/shops help Shows this message.");
						log.info("/shops Shows the info page.");
						log.info("===---Shops Help---===");
					} else if (args[0].equalsIgnoreCase("reset")) {
						configFile.delete();
						Shops.plugin.saveDefaultConfig();
						log.info("Reset the config!");
					} else if (args[0].equalsIgnoreCase("reload")) {
						Shops.plugin.reloadConfig();
						log.info("Reloaded the config!");
					} else {
						log.info("Unknown subcommand!");
					}
				}
			} else {
				Player p = (Player) sender;
				if (args.length == 0) {
					sender.sendMessage(ChatColor.DARK_AQUA + "===---" + ChatColor.GOLD + "Shops Info" + ChatColor.DARK_AQUA + "---===");
					sender.sendMessage(ChatColor.AQUA + "Shops plugin created by WolfLeader116");
					sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.RED + "/shops help " + ChatColor.AQUA + "for a list of commands.");
				} else if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("help")) {
						sender.sendMessage(ChatColor.DARK_AQUA + "===---" + ChatColor.GOLD + "Shops Help" + ChatColor.DARK_AQUA + "---===");
						sender.sendMessage(ChatColor.RED + "/shops " + ChatColor.AQUA + "Shows the info page.");
						sender.sendMessage(ChatColor.RED + "/shops help " + ChatColor.AQUA + "Shows this message.");
						if (sender.hasPermission("shops.reset")) {
							sender.sendMessage(ChatColor.RED + "/shops reset " + ChatColor.AQUA + "Resets the config.");
						}
						if (sender.hasPermission("shops.reload")) {
							sender.sendMessage(ChatColor.RED + "/shops reload " + ChatColor.AQUA + "Reloads the config.");
						}
					} else if (args[0].equalsIgnoreCase("reset")) {
						if (sender.hasPermission("shops.reset")) {
							configFile.delete();
							Shops.plugin.saveDefaultConfig();
							WolfAPI.message("Reset the config!", p, "Shops");
						} else {
							Errors.sendError(Errors.NO_PERMISSION, p, "Shops");
						}
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (sender.hasPermission("shops.reload")) {
							Shops.plugin.reloadConfig();
							WolfAPI.message("Reloaded the config!", p, "Shops");
						} else {
							Errors.sendError(Errors.NO_PERMISSION, p, "Shops");
						}
					} else {
						WolfAPI.message("Unknown subcommand!", p, "Shops");
					}
				}
			}
		}
		return false;
	}
}
