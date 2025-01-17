package com.hackclub.hccore.commands;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.PlayerData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class LocCommand implements TabExecutor {

  private final HCCorePlugin plugin;

  public LocCommand(HCCorePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(text("You must be a player to use this").color(RED));
      return true;
    }

    if (args.length == 0) {
      return false;
    }

    PlayerData data = this.plugin.getDataManager().getData(player);
    String locationName = String.join("_", Arrays.copyOfRange(args, 1, args.length));
    switch (args[0].toLowerCase()) {
      // /loc del <name>
      case "del" -> {
        if (args.length < 2) {
          sender.sendMessage(text("Please specify the location name").color(RED));
          break;
        }

        if (!data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(text("No location with that name was found").color(RED));
          break;
        }

        data.getSavedLocations().remove(locationName);
        sender.sendMessage(
            text("Removed").color(GREEN).appendSpace().append(text(locationName)).appendSpace()
                .append(text("from saved locations")));
      }

      // /loc get <name>
      case "get" -> {
        if (args.length < 2) {
          sender.sendMessage(text("Please specify the location name").color(RED));
          break;
        }
        if (!data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(text("No location with that name was found").color(RED));
          break;
        }

        Location savedLocation = data.getSavedLocations().get(locationName);
        sender.sendMessage(locationName + ": " + savedLocation.getWorld().getName() + " @ "
            + savedLocation.getBlockX() + ", " + savedLocation.getBlockY() + ", "
            + savedLocation.getBlockZ());
      }

      // /loc list
      case "list" -> {
        Map<String, Location> savedLocations = data.getSavedLocations();
        if (savedLocations.isEmpty()) {
          sender.sendMessage("You have no saved locations");
          break;
        }
        sender.sendMessage(
            text("Your saved locations (").color(AQUA).append(text(savedLocations.size()))
                .append(text("):")));
        for (Map.Entry<String, Location> entry : savedLocations.entrySet()) {
          Location savedLocation = entry.getValue();
          sender.sendMessage(
              "- " + entry.getKey() + ": " + savedLocation.getWorld().getName() + " @ "
                  + savedLocation.getBlockX() + ", " + savedLocation.getBlockY() + ", "
                  + savedLocation.getBlockZ());
        }
      }

      // /loc rename <old name> <new name>
      case "rename" -> {
        if (args.length < 3) {
          sender.sendMessage("/loc rename <old name> <new name>");
          break;
        }
        String oldName = args[1];
        String newName = String.join("_", Arrays.copyOfRange(args, 2, args.length));
        Location targetLoc = data.getSavedLocations().get(oldName);
        if (!data.getSavedLocations().containsKey(oldName)) {
          sender.sendMessage(text("No location with that name was found").color(RED));
          break;
        }
        if (data.getSavedLocations().containsKey(newName)) {
          sender.sendMessage(text("A location with that name already exists").color(RED));
          break;
        }
        data.getSavedLocations().put(newName, targetLoc);
        data.getSavedLocations().remove(oldName);
        sender.sendMessage(
            text("Renamed from").color(GREEN).appendSpace().append(text(oldName)).appendSpace()
                .append(text("to")).appendSpace().append(text(newName)));
      }

      // /loc save <name>
      case "save" -> {
        if (args.length < 2) {
          sender.sendMessage(text("Please specify the location name").color(RED));
          break;
        }
        if (data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(text("A location with that name already exists").color(RED));
          break;
        }

        Location currentLocation = player.getLocation();
        data.getSavedLocations().put(locationName, currentLocation);
        sender.sendMessage(
            text("Added").color(GREEN).appendSpace().append(text(locationName)).appendSpace()
                .append(text("(")).append(text(currentLocation.getWorld().getName())).appendSpace()
                .append(text("@")).appendSpace().append(text(currentLocation.getBlockX()))
                .append(text(",")).appendSpace().append(text(currentLocation.getBlockY()))
                .append(text(",")).appendSpace().append(text(currentLocation.getBlockZ()))
                .append(text(") to saved locations")));
      }

      // /loc share <name> <player>
      case "share" -> {
        if (args.length < 3) {
          sender.sendMessage(text(
              "Please specify the location name and the player you want to share it with").color(
              RED));
        }
        locationName = args[1];
        String recipientName = args[2];

        if (!data.getSavedLocations().containsKey(locationName)) {
          sender.sendMessage(text("No location with that name was found").color(RED));
          break;
        }
        Location sendLocation = data.getSavedLocations().get(locationName);
        // Get the player we're sending to
        Player recipient = sender.getServer().getPlayer(recipientName);
        if (recipient == null) {
          sender.sendMessage(text("No online player with that name was found").color(RED));
          break;
        }
        if (recipientName.equals(player.getName())) {
          sender.sendMessage(text("You can’t share a location with yourself!").color(RED));
          break;
        }
        PlayerData recipData = this.plugin.getDataManager().getData(recipient);
        String shareLocName = player.getName() + " " + locationName;

        if (recipData.getSavedLocations().containsKey(player.getName() + ":" + shareLocName)) {
          sender.sendMessage(text(recipientName).color(RED).appendSpace()
              .append(text("already has a location called")).appendSpace()
              .append(text(shareLocName)));
          break;
        }

        String locationString =
            "(" + sendLocation.getWorld().getName() + " @ " + sendLocation.getBlockX() + ", "
                + sendLocation.getBlockY() + ", " + sendLocation.getBlockZ() + ")";
        player.sendMessage(
            text(String.format("Shared %s with %s", locationName, recipientName)).color(GREEN));
        recipient.sendMessage(text(
            String.format("%s has shared a location: %s (%s)", player.getName(), locationName,
                locationString)).color(GREEN));
        recipData.getSavedLocations().put(player.getName() + ":" + locationName, sendLocation);
      }
      default -> {
        return false;
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
      @NotNull String alias, String[] args) {
    if (!(sender instanceof Player)) {
      return null;
    }

    List<String> completions = new ArrayList<>();
    switch (args.length) {
      // Complete subcommand
      case 1 -> {
        List<String> subcommands = Arrays.asList("del", "get", "list", "rename", "save", "share");
        StringUtil.copyPartialMatches(args[0], subcommands, completions);
      }

      // Complete location name for everything but /loc list and /loc save
      case 2 -> {
        if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("save")) {
          break;
        }

        Player player = (Player) sender;
        PlayerData data = this.plugin.getDataManager().getData(player);
        for (Map.Entry<String, Location> entry : data.getSavedLocations().entrySet()) {
          if (StringUtil.startsWithIgnoreCase(entry.getKey(), args[1])) {
            completions.add(entry.getKey());
          }
        }
      }

      // Complete online player name for /loc share
      case 3 -> {
        if (!args[0].equalsIgnoreCase("share")) {
          break;
        }

        for (Player player : sender.getServer().getOnlinePlayers()) {
          if (StringUtil.startsWithIgnoreCase(player.getName(), args[2])) {
            completions.add(player.getName());
          }
        }
      }
    }

    Collections.sort(completions);
    return completions;
  }
}
