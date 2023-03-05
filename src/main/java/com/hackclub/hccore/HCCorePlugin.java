package com.hackclub.hccore;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.hackclub.hccore.advancements.AstraAdv;
import com.hackclub.hccore.advancements.BugAdv;
import com.hackclub.hccore.advancements.ContributeAdv;
import com.hackclub.hccore.advancements.DiamondsAdv;
import com.hackclub.hccore.advancements.DragonAdv;
import com.hackclub.hccore.advancements.ElderAdv;
import com.hackclub.hccore.advancements.HubAdv;
import com.hackclub.hccore.advancements.IronGolemAdv;
import com.hackclub.hccore.advancements.MileAdv;
import com.hackclub.hccore.advancements.MusicophileAdv;
import com.hackclub.hccore.advancements.WitherAdv;
import com.hackclub.hccore.advancements.WolfAdv;
import com.hackclub.hccore.commands.AFKCommand;
import com.hackclub.hccore.commands.ColorCommand;
import com.hackclub.hccore.commands.LocCommand;
import com.hackclub.hccore.commands.NickCommand;
import com.hackclub.hccore.commands.PingCommand;
import com.hackclub.hccore.commands.SpawnCommand;
import com.hackclub.hccore.commands.StatsCommand;
import com.hackclub.hccore.listeners.AFKListener;
import com.hackclub.hccore.listeners.BeehiveInteractionListener;
import com.hackclub.hccore.listeners.NameChangeListener;
import com.hackclub.hccore.listeners.PlayerListener;
import com.hackclub.hccore.tasks.AutoAFKTask;
import com.hackclub.hccore.utils.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HCCorePlugin extends JavaPlugin {

  private DataManager dataManager;
  private ProtocolManager protocolManager;

  @Override
  public void onEnable() {
    // enable default advancement announcements, should probably leave default, but removes need to re-enable on each server
    for (World world : this.getServer().getWorlds()) {
      world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);
    }

    // Create config
    this.saveDefaultConfig();

    // Load managers
    this.dataManager = new DataManager(this);
    this.protocolManager = ProtocolLibrary.getProtocolManager();

    // Register commands

    registerCommand("afk", new AFKCommand(this));
    registerCommand("color", new ColorCommand(this));
    registerCommand("loc", new LocCommand(this));
    registerCommand("nick", new NickCommand(this));
    registerCommand("ping", new PingCommand(this));
    registerCommand("spawn", new SpawnCommand(this));
    registerCommand("stats", new StatsCommand(this));
    // disable emote commands due to Player#chat not working with colours on (recent) paper
    // current behavior is being kicked, which while funny the first time, gets old fast
    //        this.getCommand("downvote").setExecutor(new DownvoteCommand(this));
    //        this.getCommand("shrug").setExecutor(new ShrugCommand(this));
    //        this.getCommand("tableflip").setExecutor(new TableflipCommand(this));
    //        this.getCommand("upvote").setExecutor(new UpvoteCommand(this));
    //        this.getCommand("angry").setExecutor(new AngryCommand(this));
    //        this.getCommand("flippedbytable").setExecutor(new FlippedByTableCommand(this));

    // Register advancements
    this.registerAdvancements();

    // Register event listeners
    this.getServer().getPluginManager().registerEvents(new AFKListener(this), this);
    this.getServer().getPluginManager().registerEvents(new BeehiveInteractionListener(this), this);
    this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

    // Register packet listeners
    this.getProtocolManager()
        .addPacketListener(new NameChangeListener(this, ListenerPriority.NORMAL,
            PacketType.Play.Server.PLAYER_INFO));

    // Register tasks
    new AutoAFKTask(this).runTaskTimer(this,
        (long) this.getConfig().getInt("settings.auto-afk-time") * TimeUtil.TICKS_PER_SECOND,
        30 * TimeUtil.TICKS_PER_SECOND);

    // Register all the players that were online before this plugin was enabled (example
    // scenario: plugin reload) to prevent null pointer errors.
    this.getDataManager().registerAll();
  }

  @Override
  public void onDisable() {
    this.getDataManager().unregisterAll();
  }

  public DataManager getDataManager() {
    return this.dataManager;
  }

  public ProtocolManager getProtocolManager() {
    return this.protocolManager;
  }

  public AdvancementTab tab;
  public RootAdvancement root;

  private void registerAdvancements() {
    // Initialize advancement api
    UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(this);
    tab = api.createAdvancementTab("hack_club");

    // Create root display banner
    ItemStack bannerStack = new ItemStack(Material.RED_BANNER);
    BannerMeta bannerMeta = (BannerMeta) bannerStack.getItemMeta();
    List<Pattern> patterns = new ArrayList<>();
    patterns.add(new Pattern(DyeColor.WHITE, PatternType.STRIPE_RIGHT));
    patterns.add(new Pattern(DyeColor.RED, PatternType.HALF_HORIZONTAL));
    patterns.add(new Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT));
    patterns.add(new Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE));
    patterns.add(new Pattern(DyeColor.RED, PatternType.BORDER));
    bannerMeta.setPatterns(patterns);
    bannerStack.setItemMeta(bannerMeta);

    // Create root display
    AdvancementDisplay rootDisplay = new AdvancementDisplay(bannerStack, "Hack Club",
        AdvancementFrameType.TASK, false, false, 0, 3, "Beep boop beep beep boop");
    root = new RootAdvancement(tab, "root", rootDisplay, "textures/block/coal_block.png");

    AdvancementKey astraKey = new AdvancementKey(this, "astra");
    AdvancementKey bugKey = new AdvancementKey(this, "bug");
    AdvancementKey contributeKey = new AdvancementKey(this, "contribute");
    AdvancementKey diamondsKey = new AdvancementKey(this, "diamonds");
    AdvancementKey dragonKey = new AdvancementKey(this, "dragon");
    AdvancementKey elderKey = new AdvancementKey(this, "elder");
    AdvancementKey hubKey = new AdvancementKey(this, "hub");
    AdvancementKey ironGolemKey = new AdvancementKey(this, "iron_golem");
    AdvancementKey mileKey = new AdvancementKey(this, "mile");
    AdvancementKey musicophileKey = new AdvancementKey(this, "musicophile");
    AdvancementKey witherKey = new AdvancementKey(this, "wither");
    AdvancementKey wolfKey = new AdvancementKey(this, "wolf");

    CoordAdapter adapter = CoordAdapter.builder()
        .add(astraKey, 5, 3)
        .add(bugKey, 1, 2)
        .add(contributeKey, 2, 2)
        .add(diamondsKey, 1, 4)
        .add(dragonKey, 3, 4)
        .add(elderKey, 4, 4)
        .add(hubKey, 6, 4)
        .add(ironGolemKey, 2, 5)
        .add(mileKey, 5, 4)
        .add(musicophileKey, 1, 3)
        .add(witherKey, 3, 3)
        .add(wolfKey, 2, 4)
        .build();

    MusicophileAdv musicophile = new MusicophileAdv(this, root, musicophileKey, adapter);
    BugAdv bug = new BugAdv(this, root, bugKey, adapter);
    ContributeAdv contribute = new ContributeAdv(this, bug, contributeKey, adapter);
    DiamondsAdv diamonds = new DiamondsAdv(this, root, diamondsKey, adapter);
    HubAdv hub = new HubAdv(this, diamonds, hubKey, adapter);
    DragonAdv dragon = new DragonAdv(this, diamonds, dragonKey, adapter);
    WitherAdv wither = new WitherAdv(this, dragon, witherKey, adapter);
    ElderAdv elder = new ElderAdv(this, diamonds, elderKey, adapter);
    WolfAdv wolf = new WolfAdv(this, diamonds, wolfKey, adapter);
    IronGolemAdv ironGolem = new IronGolemAdv(this, wolf, ironGolemKey, adapter);
    MileAdv mile = new MileAdv(this, diamonds, mileKey, adapter);
    AstraAdv astra = new AstraAdv(this, mile, astraKey, adapter);

    // Register all advancements
    tab.registerAdvancements(root, musicophile, bug, contribute, diamonds, hub, dragon, wither,
        elder, wolf,
        ironGolem, mile, astra);
  }

  private void registerCommand(String name, CommandExecutor commandExecutor) {
    PluginCommand command = this.getCommand(name);
    if (command == null) {
      this.getLogger().severe("Command %s not found in plugin.yml".formatted(name));
      return;
    }
  }

}
