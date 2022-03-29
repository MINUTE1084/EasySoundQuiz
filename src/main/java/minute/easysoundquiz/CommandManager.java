package minute.easysoundquiz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
	public final EasySoundQuiz main;

	public CommandManager(EasySoundQuiz main_) {
		main = main_;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("esq")) {
			if (args.length == 0) {
				sender.sendMessage("\2476-------[\247eLAbility\2476]-------");
				return true;
			}

			switch (args[0].toLowerCase()) {
				case "test":
					main.gameManager.AutoGame();
					break;
			}
		}
		return true;
	}
}
