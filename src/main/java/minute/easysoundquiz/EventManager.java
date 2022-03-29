package minute.easysoundquiz;

import com.LAbility.LAPlayer;
import com.LAbility.LAbilityMain;
import com.LAbility.LuaUtility.List.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventManager implements Listener {
    private static Map<String, BukkitTask> playerList = new HashMap<>();
    public static boolean enableDisconnectOut = true;

    public static EasySoundQuiz main = EasySoundQuiz.instance;

    @EventHandler ()
    public static void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        Player p = event.getPlayer();

        String url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId();
        p.setResourcePack(url, (byte[]) null, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onChat(AsyncPlayerChatEvent event){
        if (main.gameManager.isQuizMode) {

        }
    }
}
