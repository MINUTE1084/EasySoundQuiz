package minute.easysoundquiz;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Score;

import java.io.IOException;
import java.util.ArrayList;

public class EventManager implements Listener {
    public static EasySoundQuiz main = EasySoundQuiz.instance;

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        Player p = event.getPlayer();

        String url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId();
        p.setResourcePack(url, null, false);

        p.setScoreboard(main.gameManager.scoreboard);
        main.gameManager.useImage.put(p, main.gameManager.defaultUseImage);

        if (main.gameManager.bossBar != null) main.gameManager.bossBar.addPlayer(p);
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        p.setScoreboard(main.gameManager.scoreboard);
        main.gameManager.useImage.remove(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onChat(AsyncPlayerChatEvent event){
        if (main.gameManager.currentSound != null) {
            if (main.gameManager.currentSound.realAnswer.contains(event.getMessage().replace(" ", "").toLowerCase())){
                main.gameManager.currentSound.winner = event.getPlayer();
                main.gameManager.ShowSoundInfo(main.gameManager.currentSound);

                int score = main.gameManager.useTimeBasedScore ? (int)Math.ceil(5 * (1 - main.gameManager.bossBar.getProgress())) : 1;
                Score boardScore = main.gameManager.scoreboard.getObjective("esqScore").getScore(event.getPlayer().getName());
                boardScore.setScore(score + boardScore.getScore());

                boardScore = main.gameManager.scoreboard.getObjective("esqScore2").getScore(event.getPlayer().getName());
                boardScore.setScore(score + boardScore.getScore());

                event.setMessage("\2476" + event.getMessage());
            }
        }
    }
}
