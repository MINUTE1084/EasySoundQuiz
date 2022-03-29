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

public class EventManager implements Listener {
    public static EasySoundQuiz main = EasySoundQuiz.instance;

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        Player p = event.getPlayer();

        String url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId();
        p.setResourcePack(url, null, false);

        p.setScoreboard(main.gameManager.scoreboard);
        main.gameManager.useImage.put(p, main.gameManager.defaultUseImage);
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) throws IOException {
        Player p = event.getPlayer();

        String url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId();
        p.setResourcePack(url, null, false);

        p.setScoreboard(main.gameManager.scoreboard);
        main.gameManager.useImage.remove(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onChat(AsyncPlayerChatEvent event){
        if (main.gameManager.currentSound != null) {
            if (main.gameManager.currentSound.answer.contains(event.getMessage())){
                main.gameManager.currentSound.winner = event.getPlayer();
                main.gameManager.ShowSoundInfo(main.gameManager.currentSound);

                int score = 1;
                if (main.gameManager.useTimeBasedScore) score *= (5 * (1 - main.gameManager.bossBar.getProgress()));

                Score boardScore = main.gameManager.scoreboard.getObjective("esqScore").getScore(event.getPlayer().getName());
                boardScore.setScore(score + boardScore.getScore());

                event.setMessage("\2476" + event.getMessage());
            }
        }
    }
}
