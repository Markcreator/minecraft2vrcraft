package net.Markcreator.minecraft2vrcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	public static String prefix;
	
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		prefix = "[" + pdfFile.getName() + "] ";
		this.getLogger().info(prefix + " has been enabled.");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("export")) {
			if (sender instanceof Player player) {
				String export = exportWorld(player);
				
				TextComponent message = new TextComponent(prefix + "Export successful! Click me to copy the export");
				message.setUnderlined(true);
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Copy export to clipboard")));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, export));
				player.spigot().sendMessage(message);
				return true;
			}
			else
			{
				sender.sendMessage(prefix + "Export can only be triggered by player");
			}
		}
		
		return false; 
	}

	private String exportWorld(Player player) {
		BlockVector loc = player.getLocation().toVector().toBlockVector();
		
		return loc.toString();
	}
}
