package me.chillywilly;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;

public class Main extends JavaPlugin implements TabExecutor {

    public static final String PLUGIN_ID = "talking_stick";

    public static StateFlag TALKING_STICK_FLAG;

    @Nullable
    private TalkingStick voicechatPlugin;

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("talking-stick", false);
            registry.register(flag);
            TALKING_STICK_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("talking-stick");
            if (existing instanceof StateFlag) {
                TALKING_STICK_FLAG = (StateFlag) existing;
            } else {
                getLogger().info("Some other plugin has the worldguard flag, good luck finding it");
            }
        }
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voicechatPlugin = new TalkingStick(this);
            service.registerPlugin(voicechatPlugin);
            getLogger().info("Registered Talking Stick Voicechat Add-on");
        }

        getCommand("talkingstick").setExecutor(this);
        getCommand("talkingstick").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            getLogger().info("Unregistered talking stick voicechat add-on");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("talking_stick.admin")) {
            if (args.length >= 1) {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        reloadConfig();
                        voicechatPlugin.reloadConfig();
                        sender.sendMessage("Reloaded Config!");
                        return true;
                    case "setitem":
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            ItemStack heldItem = null;
                            if (player.getInventory().getItemInMainHand() != null) {
                                heldItem = player.getInventory().getItemInMainHand();
                            } else if (player.getInventory().getItemInOffHand() != null) {
                                heldItem = player.getInventory().getItemInOffHand();
                            } else {
                                player.sendMessage("Please hold which item you want the talking stick to be");
                            }

                            Material itemMaterial = heldItem.getType();
                            Integer itemModelData = 0;

                            if (heldItem.hasItemMeta() && heldItem.getItemMeta().hasCustomModelData()) {
                                itemModelData = heldItem.getItemMeta().getCustomModelData();
                            }

                            getConfig().set("talking-stick-item", itemMaterial.toString());
                            getConfig().set("talking-stick-custom-model-data", itemModelData);

                            saveConfig();
                            sender.sendMessage("Update Config!");

                            reloadConfig();
                            voicechatPlugin.reloadConfig();
                            sender.sendMessage("Reloaded Config!");
                            return true;
                        } else {
                            sender.sendMessage("You must be a player to do this command");
                            return true;
                        }
                    default:
                        sender.sendMessage("Choose one of the options, (reload, setitem)");
                        return true;
                }
            } else {
                sender.sendMessage("Choose one of the options, (reload, setitem)");
                return true;
            }
        } else {
            sender.sendMessage("You do not have permission to run this command!");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ret = new ArrayList<>();
        if (args.length == 1) {
            ret.add("reload");
            ret.add("setitem");
            return ret;
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
