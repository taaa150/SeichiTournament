package seichitournament;

import com.mysql.fabric.xmlrpc.base.*;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.lang.reflect.Array;

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
			if (element.equals(checkElement)) {
				return true;
			}
		}
		return false;
	}
}
