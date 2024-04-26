package me.chillywilly;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;

public class TalkingStick implements VoicechatPlugin {

	Main plugin;

	Material talkingStickMaterial;
	Integer talkingStickCustomID;

	public TalkingStick(Main plugin) {
		this.plugin = plugin;

		String configStickItem = plugin.getConfig().getString("talking-stick-item");

		if (Material.getMaterial(configStickItem) == null) {
			plugin.getLogger().info("Talking Stick item is incorrectly configured, disabling plugin");
			plugin.getPluginLoader().disablePlugin(plugin);
			return;
		}

		talkingStickMaterial = Material.getMaterial(configStickItem);

		talkingStickCustomID = plugin.getConfig().getInt("talking-stick-custom-model-data");

		if (talkingStickCustomID == 0) {
			plugin.getLogger().info("Talking Stick Custom Model Data not set, defaulting to regular " + talkingStickMaterial);
		}

		plugin.getLogger().info("Registered talking stick as a " + talkingStickMaterial);
		if (talkingStickCustomID != 0 ) {
			plugin.getLogger().info("Registered custom model data for talking stick as " + talkingStickCustomID);
		}
	}

	@Override
	public String getPluginId() {
		return Main.PLUGIN_ID;
	}

	@Override
	public void initialize(VoicechatApi api) {
		
	}

	public void reloadConfig() {
		String configStickItem = plugin.getConfig().getString("talking-stick-item");

		if (Material.getMaterial(configStickItem) == null) {
			plugin.getLogger().info("Talking Stick item is incorrectly configured, disabling plugin");
			plugin.getPluginLoader().disablePlugin(plugin);
			return;
		}

		talkingStickMaterial = Material.getMaterial(configStickItem);

		talkingStickCustomID = plugin.getConfig().getInt("talking-stick-custom-model-data");

		plugin.getLogger().info("Registered talking stick as a " + talkingStickMaterial);
		if (talkingStickCustomID != 0 ) {
			plugin.getLogger().info("Registered custom model data for talking stick as " + talkingStickCustomID);
		}
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(MicrophonePacketEvent.class, this::voiceChatTalk);
	}


	public void voiceChatTalk(MicrophonePacketEvent event) {
		Player player = (Player) event.getSenderConnection().getPlayer().getEntity();		

		if (event.getSenderConnection() == null) { //ya it brokey
			return;
		}

		if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player)) { //Not a bukkit player
			return;
		}

		if (player.hasPermission("talking_stick.bypass")) { //Bypass Permission
			return;
		}

		Location loc = new Location(BukkitAdapter.adapt(player.getWorld()), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
		
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(loc);

		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		if (!set.testState(localPlayer, Main.TALKING_STICK_FLAG)) {
			return; //Player is not in a region with the talking stick flag, or it is false
		}

		if (player.getInventory().getItemInMainHand().getType() == talkingStickMaterial || player.getInventory().getItemInOffHand().getType() == talkingStickMaterial) { //item in main or offhand is the same material as a talking stick
			if (talkingStickCustomID != 0) { //Custom model ID is set in config
				if (player.getInventory().getItemInMainHand().hasItemMeta() && player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelData()) { //Check Main Hand
					if (player.getInventory().getItemInMainHand().getItemMeta().getCustomModelData() == talkingStickCustomID) {
						//player has talking stick
						return;
					}
				}

				if (player.getInventory().getItemInOffHand().hasItemMeta() && player.getInventory().getItemInOffHand().getItemMeta().hasCustomModelData()) {
					if (player.getInventory().getItemInOffHand().getItemMeta().getCustomModelData() == talkingStickCustomID) {
						//player has talking stick
						return;
					}
				}

				event.cancel();
			}
		} else {
			event.cancel();
		}
	}
}
