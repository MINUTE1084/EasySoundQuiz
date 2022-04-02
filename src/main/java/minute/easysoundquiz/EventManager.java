package minute.easysoundquiz;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

        String url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId() + EasySoundQuiz.instance.randomint;
        if (EasySoundQuiz.instance.fileSize <= 250) p.setResourcePack(url, null, false);
        else {
            p.sendMessage("\247c리소스팩 크기가 250MB를 초과합니다! 다음 링크를 클릭해서 파일을 다운로드 후, 수동 적용해주세요.");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw @p {\"text\":\"[링크]\",\"underlined\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}");
        }

        p.setScoreboard(main.gameManager.scoreboard);
        main.gameManager.useImage.put(p, ConfigManager.DEFAULT_USE_IMAGE);

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

                int score = ConfigManager.USE_TIME_BASED_SCORE ? (int)Math.ceil(ConfigManager.TIME_BASED_SCORE * (1 - main.gameManager.bossBar.getProgress())) : 1;
                Score boardScore = main.gameManager.scoreboard.getObjective("esqScore").getScore(event.getPlayer().getName());
                boardScore.setScore(score + boardScore.getScore());

                boardScore = main.gameManager.scoreboard.getObjective("esqScore2").getScore(event.getPlayer().getName());
                boardScore.setScore(score + boardScore.getScore());

                event.setMessage("\2476" + event.getMessage());
            }
        }
    }

    @EventHandler
    public static void onFirework(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Firework) event.setCancelled(true);
    }
}
