package minute.easysoundquiz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabManager implements TabCompleter {
    public final EasySoundQuiz main;

    public TabManager(EasySoundQuiz main_) {
        main = main_;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> blank = new ArrayList<String>();
        if (args.length == 1) {
            List<String> basicCommand = new ArrayList<String>();

            if (commandSender instanceof Player) {
                basicCommand.add("image");
                if (commandSender.isOp() || ConfigManager.CAN_USE_LIST) basicCommand.add("list");
                if (commandSender.isOp()) {
                    basicCommand.add("quiz");
                    basicCommand.add("play");
                }
            }

            if (commandSender.isOp()) {
                basicCommand.add("start");
                basicCommand.add("stop");
                basicCommand.add("skip");
                basicCommand.add("hint");
                basicCommand.add("score");
                basicCommand.add("play");
                basicCommand.add("reroll");
                basicCommand.add("reset");
                basicCommand.add("reload");
            }

            return basicCommand;
        }
        return blank;
    }
}
