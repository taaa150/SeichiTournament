package seichitournament;

import java.util.*;

import com.github.ucchyocean.lc.*;
import com.github.ucchyocean.lc.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SeichiTournament extends JavaPlugin {

	static SeichiTournament plugin;
	static LunaChatAPI lunaChatAPI;

	private static Player player = null ;
	private int TeamMAX = 0 ;
	private int GameTime = 0 ;
	private boolean EventMode = false ;

	// 設定ファイルを読み込む
	private FileConfiguration conf = getConfig();

	@Override
	public void onEnable() {
		// もし設定ファイルがまだなければ、デフォルトの設定を保存する
		saveDefaultConfig();

		TeamMAX = conf.getInt("MemberMAX");
		GameTime = conf.getInt("GameTime");
		EventMode = conf.getBoolean("EventMode");

		plugin = this;

	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		lunaChatAPI = Util.getLunaChatAPI();
		if (!(sender instanceof Player)) {
			sender.sendMessage("コマンドはplayerから入力してください");
			return false;
		}
        // プレイヤーがコマンドを投入した際の処理...
		if (cmd.getName().equalsIgnoreCase("seichitourn")) {

			//参加者データの読み込み・整理
			player = (Player)sender;
			String[][] teams = new String[21][TeamMAX];
			Map<String, Integer> data = new HashMap<>();
			for (Map<?, ?> map : conf.getMapList("member")) {
				map.forEach((key, value) -> data.put(Util.autoCast(key), Util.autoCast(value)));
			}
			Bukkit.getLogger().info(conf.getMapList("member").toString());
			List<String> team0member = new ArrayList<>();

			for (String playerName : data.keySet()) {
				int teamNum = data.get(playerName);

				if (0 < teamNum && teamNum < 21) {
					if (!Util.isExistsNullElements(teams[teamNum])) {
						sender.sendMessage(ChatColor.RED + "[ERROR] チームNo" + teamNum + "が上限を超えています");
					} else if (!Util.isExistsSameFactor(teams[teamNum], playerName)) {
						teams[teamNum] = Util.addFactorLast(teams[teamNum], playerName);
					}
				} else if (teamNum == 0) {
					team0member.add(playerName);
				} else {
					sender.sendMessage(ChatColor.RED + "[ERROR] 「" + playerName + "」のチーム指定が不正です");
				}
			}

			if (args.length == 0) {
				sender.sendMessage("コマンド「seichitourn」は公式イベント「整地大会」用のコマンドです。");
				sender.sendMessage("「/seichitourn members」：参加者の一覧を表示します。");
				sender.sendMessage("「/seichitourn addmember」：参加者を追加・チーム移動する為のコマンド");
				sender.sendMessage("「/seichitourn removemember」：参加者を削除する為のコマンド");
				sender.sendMessage("「/seichitourn setregion」：チーム情報をサーバに反映します");
				sender.sendMessage("「/seichitourn teleport」：参加者をテレポートさせます");
				sender.sendMessage("「/seichitourn gamestart」：試合を開始します");
				sender.sendMessage("「/seichitourn setting」：試合設定を確認・変更します");
			} else if(args[0].equals("members")) {
				//参加者一覧表示コマンド
				if (args.length == 1) {
					//全参加者を表示
					sender.sendMessage("全参加者:" + data.keySet());
					sender.sendMessage("各チームごとの参加者については、コマンド末尾にチームNo[1～20]を入れてください");
				} else {
					if (!(Util.isInt(args[1]))) {
						sender.sendMessage(ChatColor.RED + "[ERROR] <チームNo>の項目は「数字(1～20)」で指定する必要があります。");
					} else if (!(0 <= Util.toInt(args[1]) && Util.toInt(args[1]) <= 20 )) {
						sender.sendMessage(ChatColor.RED + "[ERROR] <チームNo>の項目は1～20の番号で指定してください。");
					} else {
						//チーム参加者を表示
						StringBuilder membersforC = new StringBuilder("チームNo" + args[1] + ":");
						if (Util.toInt(args[1]) == 0) {
							//1の時はArrayListを使っているので別表示
							for (String playerName : team0member) {
								membersforC.append(playerName).append(", ");
							}
						} else {
							for (int i = 0 ; i < TeamMAX ; i++) {
								if (teams[Util.toInt(args[1])][i] != null) {
									membersforC.append(teams[Util.toInt(args[1])][i]).append(", ");
								}
							}
						}
						sender.sendMessage(membersforC.toString());
					}
				}
			} else if (args[0].equals("addmember")) {
				//メンバー追加コマンド
				if (!(args.length == 3)) {
					sender.sendMessage("コマンド形式は「/SeichiTourn addmember <追加したいユーザー名> <参加チーム>」です。");
					sender.sendMessage("<参加チーム>の項目は「数字(1～20)」で指定する必要があります。");
					sender.sendMessage("チームが決まっていない場合は<参加チーム>の項目を「0」としてください。");
				} else if (!(Util.isInt(args[2]))) {
					sender.sendMessage(ChatColor.RED + "[ERROR] <参加チーム>の項目は「数字(1～20)」で指定する必要があります。");
					sender.sendMessage("チームが決まっていない場合は<参加チーム>の項目を「0」としてください。");
				} else if (!(0<= Util.toInt(args[2]) && Util.toInt(args[2]) <= 20)) {
					sender.sendMessage(ChatColor.RED + "[ERROR] <参加チーム>の項目は1～20の範囲で指定する必要があります。");
					sender.sendMessage("チームが決まっていない場合は<参加チーム>の項目を「0」としてください。");
				} else {
					String playerName = args[1];
					int teamNum = Integer.parseInt(args[2]);
					if (teamNum == 0) {
						//1の時だけArrayListなので判定が特殊
						team0member.add(playerName);
						Util.addMemberToConf(conf, playerName, teamNum);
						saveConfig();
						sender.sendMessage("プレイヤー" + playerName + "はチームNo." + teamNum + "に参加しました。");
						return true;
					}
					if (!Util.isExistsNullElements(teams[teamNum])) {
						sender.sendMessage(ChatColor.RED + "[ERROR] 選択したチームNo." + teamNum +
								"が上限人数の" + TeamMAX + "人に達している為登録できませんでした。");
					} else if (Util.isExistsSameFactor(teams[teamNum], playerName)) {
						sender.sendMessage(ChatColor.RED + "[ERROR] 選択したチームNo." + teamNum +
								"にプレイヤー" + playerName + "はすでに参加しています。");
					} else {
						Util.addFactorLast(teams[teamNum], playerName);
						Util.addMemberToConf(conf, playerName, teamNum);
						saveConfig();
						sender.sendMessage("プレイヤー" + playerName + "はチームNo." + teamNum + "に参加しました。");
					}
				}
			} else if (args[0].equals("setregion")) {
				//各保護・ルナチャットにメンバーを設定
				for (int i = 1; i <= 20 ; i++){
					player.chat("/rg removemember team_"+ i +" -a");
					lunaChatAPI.removeChannel("team" + i);
					lunaChatAPI.createChannel("team" + i);
					for (int j = 0; j < TeamMAX; j++){
						if (!(teams[i][j] == null)) {
							player.chat("/rg addmember team_"+ i +" " + teams[i][j]);
							Channel channel = lunaChatAPI.getChannel("team" + i);
							channel.addMember(ChannelPlayer.getChannelPlayer(teams[i][j]));
						}
					}
				}
			} else if (args[0].equals("teleport")) {
				//参加者テレポートコマンド 57,-55
				for (int i = 1 ; i < 21 ; i ++) {
					for (int i2 = 0 ; i2 < TeamMAX ; i2 ++) {
						String A = teams[i][i2];
						int Xpos = 57 - ((i - 1) / 5) * 31 ;
						int Zpos = -56 + ((i - 1) % 5) * 25 ;
						if (!(teams[i][i2] == null)) {
							player.chat("/tp "+ A + " " + Xpos + " 128 " + Zpos);
						}
					}
				}
			} else if (args[0].equals("gamestart")) {
				//試合進行コマンド
				for(int loops = 10; loops > -1 ; loops --){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (Player allplayer : Bukkit.getOnlinePlayers()) {
						if (!(loops == 0)) {
							allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
							allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
							allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
							allplayer.sendMessage(ChatColor.GOLD + "試合開始まであと" + loops + "秒");
						} else {
							allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
							allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
							allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
							allplayer.sendMessage(ChatColor.RED +"試合開始！！");
						}
					}
				}

				//スケマからマップをコピーする処理もいれる？
				for (int loops = 1; loops<17; loops ++) {
					//buildフラグ解禁、試合開始
					player.chat("/rg flag team_"+ loops +" build allow");
				}
				//試合中は処理停止
				if (GameTime > 5) {//試合時間が６分以上の場合。
			        new BukkitRunnable() {
			            @Override
			            public void run() {
							for (Player allplayer : Bukkit.getOnlinePlayers()) {
								allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
								allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
								allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
								allplayer.sendMessage(ChatColor.GOLD + "残り5分です！");
							}
			            }
			        }.runTaskLater(this, GameTime * 1200 - 6000);

			        new BukkitRunnable() {
			            @Override
			            public void run() {
							for (Player allplayer : Bukkit.getOnlinePlayers()) {
								allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
								allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
								allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
								allplayer.sendMessage(ChatColor.GOLD + "残り" + ChatColor.RED + "1分" + ChatColor.GOLD + "です！");
							}
			            }
			        }.runTaskLater(this, GameTime * 1200 - 1200);

			        new BukkitRunnable() {
			            @Override
			            public void run() {
							for (int loops = 10; loops > -1 ; loops --) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								for (Player allplayer : Bukkit.getOnlinePlayers()) {
									if (!(loops == 0)) {
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.sendMessage(ChatColor.GOLD + "試合終了まであと" +ChatColor.RED + loops + ChatColor.GOLD+ "秒");
									} else {
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.sendMessage(ChatColor.RED +"試合終了！！");
									}
								}
							}
			            }
			        }.runTaskLater(this, GameTime*1200 - 200);
				} else {//５分以下の場合
			        new BukkitRunnable() {
			            @Override
			            public void run() {
							for(int loops = 10; loops > -1 ; loops --){
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								for (Player allplayer : Bukkit.getOnlinePlayers()) {
									if (!(loops == 0)) {
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.sendMessage(ChatColor.GOLD + "試合終了まであと" +ChatColor.RED + loops + ChatColor.GOLD+ "秒");
									} else {
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.playSound(allplayer.getLocation(), Sound.BLOCK_NOTE_HARP, 2f, 1f);
										allplayer.sendMessage(ChatColor.RED +"試合終了！！");
									}
								}
							}
			            }
			        }.runTaskLater(this, GameTime*1200 - 20);
				}
				//試合終了
		        new BukkitRunnable() {
		            @Override
		            public void run() {
						for (int loops = 1; loops< 21; loops ++) {
							//buildフラグ無効化、
							player.chat("/rg flag team_"+ loops +" build deny");
						}

						//結果集計
						int[] Scores = new int[21];
						for(int i = 1 ; i <  21 ; i++){
							Scores[i] = resultchecker(i) ;
						}

						//トップ5抽出
						int[] TOP5 = new int[5];
						TOP5 = ranking (Scores);

						sender.sendMessage("【集計結果】");
						sender.sendMessage("第一位:" + "Team" + TOP5[0] + " (スコア:"+ Scores[TOP5[0]] + ")");
						sender.sendMessage("第二位:" + "Team" + TOP5[1] + " (スコア:"+ Scores[TOP5[1]] + ")");
						sender.sendMessage("第三位:" + "Team" + TOP5[2] + " (スコア:"+ Scores[TOP5[2]] + ")");
						sender.sendMessage("第四位:" + "Team" + TOP5[3] + " (スコア:"+ Scores[TOP5[3]] + ")");
						sender.sendMessage("第五位:" + "Team" + TOP5[4] + " (スコア:"+ Scores[TOP5[4]] + ")");

						//コマンド入力実施者に結果通達、もしログアウト等で送信できない場合は全体通知を実施
		            }
		        }.runTaskLater(this, GameTime*1200);

			} else if (args[0].equals("setting")) {
				boolean settingerror = true;
				//gametime等の設定コマンド(ymlには反映しない一時的なもの)
				if (args.length == 3) {
					if (args[1].equals("TeamMAX")) {
						if (Util.isInt(args[2])) {
							TeamMAX = Util.toInt(args[2]) ;
							sender.sendMessage("チームの最大人数を" + args[2] + "人に設定しました。");
							settingerror = false;
						}
					} else if(args[1].equals("GameTime")) {
						if (Util.isInt(args[2])) {
							GameTime = Util.toInt(args[2]) ;
							sender.sendMessage("試合時間を" + args[2] + "分に設定しました。");
							settingerror = false;
						}
					} else if(args[1].equals("EventMode")) {
						if (args[2].equals("true")) {
							EventMode = true ;
							sender.sendMessage("特殊イベントの発生を有効にしました。");
							settingerror = false ;
						} else if(args[2].equals("false")) {
							EventMode = false ;
							sender.sendMessage("特殊イベントの発生を無効にしました。");
							settingerror = false ;
						}
					}
				}
				if (settingerror) {
					//入力不備メッセ
					sender.sendMessage("settingコマンドは「TeamMAX(チーム人数設定)」「GameTime(試合時間(分単位))」「EventMode(特殊イベント有無)」に対応しています。");
					sender.sendMessage("「/seichitourn setting <設定項目> <設定値>」の形で入力してください。");
					sender.sendMessage("<設定値>は、チーム人数・試合時間は[数字],特殊イベントは[true/false]で入力してください。");
					sender.sendMessage("【現在の設定】チーム人数:" + TeamMAX + "人");
					sender.sendMessage("【現在の設定】試合時間:" + GameTime + "分");
					sender.sendMessage("【現在の設定】特殊イベント:" + EventMode);
				}
			}
			return true;
		}
		return false;
	}

	static int resultchecker(int i){
		int score = 256 ;
		//結果の確認処理
		int Yscore = 128 ;
		int Xcheck = 52 ;
		int Zcheck = -58 ;

		Xcheck = Xcheck - ((i - 1) / 5) * 31 ;
		Zcheck = Zcheck + ((i - 1) % 5) * 25 ;

		outpoint:for (;Yscore > 0; Yscore --) {
			for (int ix = 1 ; ix < 17 ; ix ++) {
				for (int iz = 1 ; iz < 17 ; iz ++) {
					if (!(player.getWorld().getBlockAt(Xcheck,Yscore,Zcheck).getType() == Material.AIR)) {
						score = Yscore ;
						//System.out.println("Result:Team" + i + ":" + score);
						break outpoint;
					}
					//System.out.println("Z: " + Zcheck);
					Zcheck ++;
					if (iz == 16) {
						Zcheck = -58 + ((i - 1) % 5) * 25 ;
					}
				}
				//System.out.println("X: " + Xcheck );
				Xcheck -- ;
				if (ix == 16) {
					Xcheck = 52 - ((i - 1) / 5) * 31 ;
				}
			}
			//System.out.println("Y:"+ Yscore);
		}
		return score ;
	}

	private static int[] ranking(int[] i){
		int[] scores = new int[21];
		scores = i ;
		scores[0] = 256 ;
		int no1 = 0;
		int no2 = 0;
		int no3 = 0;
		int no4 = 0;
		int no5 = 0;
		int[] result = new int[5];

		for(int ir = 1 ; ir < 21 ; ir ++){
			if(scores[ir] < scores[no1]){
				no5 = no4;
				no4 = no3;
				no3 = no2;
				no2 = no1;
				no1 = ir;
			}else if (scores[ir] < scores[no2]){
				no5 = no4;
				no4 = no3;
				no3 = no2;
				no2 = ir;
			}else if (scores[ir] < scores[no3]){
				no5 = no4;
				no4 = no3;
				no3 = ir;
			}else if (scores[ir] < scores[no4]){
				no5 = no4;
				no4 = ir;
			}else if (scores[ir] < scores[no5]){
				no5 = ir;
			}
		}
		result[0] = no1;
		result[1] = no2;
		result[2] = no3;
		result[3] = no4;
		result[4] = no5;

		return result;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		getLogger().info("onDisableメソッドが呼び出されました");

	}


}
