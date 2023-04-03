package com.hackclub.hccore.playerMessages;

import com.hackclub.hccore.HCCorePlugin;
import com.hackclub.hccore.playerMessages.tags.ConditionalResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class WelcomeMessage {

  final static String minimsgSource = """
      <gold><i>Welcome to <b>HackCraft Vanilla</b></i></gold>
        To keep everything <b>fair</b> and <b>fun</b>,
        please read the <b><red><click:run_command:'/rules'><hover:show_text:'Click to Run <red>/rules</red>'>/rules</hover></click></red></b>,
      <ifslack>  and join the <b><aqua><click:run_command:'/slack'><hover:show_text:'Click to Run <aqua>/slack</aqua>'>/slack</hover></click></aqua></b>
      </ifslack><gray>To view this again at any time, use <click:run_command:'/welcome'><hover:show_text:'Click to Run <gray>/welcome</gray>'>/welcome</hover></click></gray>""";


  public static void send(CommandSender sender) {
    send(sender, HCCorePlugin.getPlugin(HCCorePlugin.class).getSlackBot() != null);
  }


  public static void send(CommandSender sender, Boolean withSlack) {
    Component component = MiniMessage.miniMessage()
        .deserialize(minimsgSource, ConditionalResolver.conditionalTag("ifslack", withSlack));
    sender.sendMessage(component);
  }
}
