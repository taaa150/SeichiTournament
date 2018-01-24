package seichitournament;

import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Util {
	public Util() {
	}

	public static boolean isInt(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	public static int toInt(String s) {
		return Integer.parseInt(s);
	}

	//ワールドガードAPIを返す
	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = SeichiTournament.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }

	    return (WorldGuardPlugin) plugin;
	}
}
