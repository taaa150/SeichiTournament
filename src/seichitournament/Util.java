package seichitournament;

import com.github.ucchyocean.lc.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.*;

public class Util {
	static boolean isInt(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	static int toInt(String s) {
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

	/**
	 * LunaChatAPIを返します。
	 * @return LunaChatAPIインスタンス
	 */
	static LunaChatAPI getLunaChatAPI() {
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("LunaChat")) {
			LunaChat lunaChat = (LunaChat) Bukkit.getServer().getPluginManager().getPlugin("LunaChat");
			return lunaChat.getLunaChatAPI();
		}
		throw new NullPointerException("LunaChatが有効化されていないようです");
	}

	/**
	 * String型配列の最後(nullが挿入されている前提)に指定した要素を追加します。
	 * 前から順に詰めていることが前提です。
	 *
	 * @param array 要素を追加したい配列
	 * @param addElement 追加したい要素
	 * @return 要素を追加された後の配列
	 *
	 * @author karayuu
	 */
	static String[] addFactorLast (String[] array, String addElement) {
		int count = 0;
		for (String check : array) {
			if (check == null) {
				array[count] = addElement;
				break;
			}
			count++;
		}
		return array;
	}

	/**
	 * 同様の要素が配列内に存在するか返します。
	 *
	 * @param checkArray 判定する配列
	 * @param checkElement 判定する文字列
	 * @return 存在する: true / 存在しない: false
	 *
	 * @author karayuu
	 */
	static boolean isExistsSameFactor(String[] checkArray, String checkElement) {
		for (String element : checkArray) {
		    if (element == null) {
		        continue;
            }
			if (element.equals(checkElement)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 配列内にNull要素が存在するか返します。
	 * (trueを返す->配列に空きがある)
	 *
	 * @param checkArray 判定する配列
	 * @return 1つでも存在する: true / 存在しない: false
	 *
	 * @author karayuu
	 */
	static boolean isExistsNullElements(String[] checkArray) {
		for (String element: checkArray) {
			if (element == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * configファイルのmemberセクションにプレイヤー名を追加します。
	 *
	 * @param conf Config
	 * @param addName 追加したいプレイヤー名
	 * @param teamNum 追加したいプレイヤーのチーム番号
	 */
	static void addMemberToConf(FileConfiguration conf, String addName, int teamNum) {
		if (!conf.isConfigurationSection("Member." + addName)) {
			conf.createSection("Member." + addName);
		}
		conf.set("Member." + addName, teamNum);
	}
}
