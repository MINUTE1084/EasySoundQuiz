package minute.easysoundquiz;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandManager implements CommandExecutor {
	public final EasySoundQuiz main;

	public CommandManager(EasySoundQuiz main_) {
		main = main_;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("esq")) {
			if (args.length == 0) {
				sender.sendMessage("\2472-------[\247aEasySoundQuiz\2472]-------");
				sender.sendMessage("\2472/esq \247aadmin \247f: \247e관리자 용 명령어를 확인합니다."); //
				sender.sendMessage("\2472/esq \247aimage \247f: \247e문제 종료 시, 곡 이미지를 출력할 것인지 정합니다."); //
				sender.sendMessage("\2472/esq \247alist \247f: \247e현재 서버에 등록된 문제 리스트를 확인합니다."); //
				return true;
			}

			switch (args[0].toLowerCase()) {
				case "image":
					if (sender instanceof Player player) {
						main.gameManager.useImage.replace(player, !main.gameManager.useImage.get(player));
						if (main.gameManager.useImage.get(player))
							sender.sendMessage("\2472[\247aEasySoundQuiz\2472] \247a문제 종료 시, 곡 이미지를 출력합니다.");
						else sender.sendMessage("\2472[\247aEasySoundQuiz\2472] \247a문제 종료 시, 곡 이미지를 출력하지 않습니다.");
					} else {
						sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c해당 명령어는 플레이어만 사용 가능합니다.");
					}
					break;
				case "admin":
					if (args.length == 1) {
						sender.sendMessage("\2472-------[\247aAdmin Command\2472]-------");
						sender.sendMessage("\2472/esq \247astart <문제 수> \247f: \247e해당 문제 수 만큼 게임을 자동 진행합니다."); // OK
						sender.sendMessage("\2472/esq \247astop \247f: \247e현재 진행중인 게임을 종료합니다."); // OK
						sender.sendMessage("\2472/esq \247askip \247f: \247e현재 문제를 스킵합니다."); // OK
						sender.sendMessage("\2472/esq \247ahint \247f: \247e현재 문제에 대한 힌트를 공지합니다."); // OK
						sender.sendMessage("\2472/esq \247aquiz \247f: \247e문제를 선택하여 출제합니다. (게임이 진행중이지 않을 때)"); //
						sender.sendMessage("\2472/esq \247ascore \247f: \247e현재 점수에 기반한 게임 진행상황을 공지합니다."); //
						sender.sendMessage("\2472/esq \247aplay \247f: \247e문제를 선택하여 해당 문제의 사운드를 모두에게 재생합니다.");
						sender.sendMessage("\2472/esq \247areset \247f: \247e점수를 초기화합니다."); // OK
						sender.sendMessage("\2472/esq \247areload \247f: \247e플러그인을 초기화하고, 리소스팩을 재생성하여 배포합니다."); // OK
						return true;
					}
					break;
				case "quiz":
					if (sender instanceof Player player) {
						if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
						if (main.gameManager.isAutoGame()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c게임이 진행 중입니다."); return true;}
						GUIManager.OpenSongList(player, "", 0);
						new BukkitRunnable(){
							@Override
							public void run() {
								SoundInfo info = GUIManager.GetSelectedInfo(player);
								if (info != null) {
									GUIManager.RemoveSelectedInfo(player);
									main.gameManager.StartQuiz(info);
									cancel();
								}
							}
						}.runTaskTimer(EasySoundQuiz.plugin, 0, 1);
					} else {
						sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c해당 명령어는 플레이어만 사용 가능합니다.");
					}
					break;
				case "play":
					if (sender instanceof Player player) {
						if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
						if (main.gameManager.isAutoGame()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c게임이 진행 중입니다."); return true;}
						GUIManager.OpenSongList(player, "", 0);
						new BukkitRunnable(){
							@Override
							public void run() {
								SoundInfo info = GUIManager.GetSelectedInfo(player);
								if (info != null) {
									GUIManager.RemoveSelectedInfo(player);
									main.gameManager.PlaySound(info);
									cancel();
								}
							}
						}.runTaskTimer(EasySoundQuiz.plugin, 0, 1);
					} else {
						sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c해당 명령어는 플레이어만 사용 가능합니다.");
					}
					break;
				case "list":
					if (sender instanceof Player player) {
						if (!player.isOp() && !ConfigManager.CAN_USE_LIST) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
						GUIManager.OpenSongList(player, "", 0);
						new BukkitRunnable(){
							@Override
							public void run() {
								SoundInfo info = GUIManager.GetSelectedInfo(player);
								if (info != null) {
									GUIManager.RemoveSelectedInfo(player);
									cancel();
								}
							}
						}.runTaskTimer(EasySoundQuiz.plugin, 0, 1);
					} else {
						sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c해당 명령어는 플레이어만 사용 가능합니다.");
					}
					break;
				case "start":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					if (main.gameManager.isAutoGame()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c게임이 진행 중입니다."); return true;}
					if (args.length >= 2) {
						try {
							main.gameManager.maxGame = Integer.parseInt(args[1]);
							main.gameManager.AutoGame();
						} catch (Exception e) {
							sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c유효한 값이 아닙니다.");
							return true;
						}
					}
					break;
				case "stop":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					if (!main.gameManager.isAutoGame()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c게임 진행 중이 아닙니다."); return true;}
					Bukkit.broadcastMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자가 게임을 중단했습니다.");
					main.gameManager.ForceEnd();
					main.getServer().getScheduler().cancelTasks(EasySoundQuiz.plugin);
					break;
				case "skip":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					if (!main.gameManager.isQuizMode()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c문제 출제 상태가 아닙니다."); return true;}
					Bukkit.broadcastMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자가 해당 문제를 스킵했습니다.");
					main.gameManager.TimeOver();
					break;
				case "hint":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					if (!main.gameManager.isQuizMode()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c문제 출제 상태가 아닙니다."); return true;}
					Bukkit.broadcastMessage("\2476[\247eEasySoundQuiz\2476] \247e관리자가 힌트를 공지합니다.");
					main.gameManager.ShowHint(main.gameManager.currentSound);
					break;
				case "score":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					main.gameManager.ShowPlace(false);
					break;
				case "reset":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					Bukkit.broadcastMessage("\2472[\247aEasySoundQuiz\2472] \247a관리자가 점수를 초기화했습니다.");
					main.gameManager.ResetScore();
					break;
				case "reload":
					if (!sender.isOp()) {sender.sendMessage("\2474[\247cEasySoundQuiz\2474] \247c관리자만 사용 가능합니다."); return true;}
					main.Reset();
					Bukkit.broadcastMessage("\2472[\247aEasySoundQuiz\2472] \247aReload Complete");
					break;
			}
		}
		return true;
	}
}
