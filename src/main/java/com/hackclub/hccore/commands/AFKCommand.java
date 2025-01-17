package com.hackclub.hccore.commands;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import com.hackclub.hccore.commands.general.ArgumentlessCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AFKCommand extends ArgumentlessCommand implements CommandExecutor {

  private final HCCorePlugin plugin;

  public AFKCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(text("You must be a player to use this").color(RED));
      return true;
    }

    // Toggle player's AFK status
    PlayerData data = this.plugin.getDataManager().getData(player);
    data.setAfk(!data.isAfk());

    return true;
  }
}
