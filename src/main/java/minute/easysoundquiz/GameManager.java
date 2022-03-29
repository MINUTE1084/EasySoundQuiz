package minute.easysoundquiz;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameManager {
    public Scoreboard scoreboard;
    public ArrayList<Integer> shuffledQuizIndex;
    public SoundInfo currentSound;

    public KeyedBossBar bossBar = null;

    public int timeEnd = 1200;
    public int timeHint = 200;

    private BukkitTask quizTask;
    public HashMap<Player, Boolean> useImage;
    public boolean defaultUseImage = true;
    public boolean useTimeBasedScore = true;

    public GameManager() {
        useImage = new HashMap<>();
        shuffledQuizIndex = new ArrayList<>();

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("esqScore", "dummy", "점수");
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        
        for (int i = 0; i < EasySoundQuiz.instance.soundData.size(); i++) shuffledQuizIndex.add(i);

        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            int randomIndex = random.nextInt(0, shuffledQuizIndex.size());
            final int temp = shuffledQuizIndex.get(0);
            shuffledQuizIndex.set(0, shuffledQuizIndex.get(randomIndex));
            shuffledQuizIndex.set(randomIndex, temp);
        }
    }
    
    int indexR = 0;
    public void StartRandomQuiz() {
        currentSound = EasySoundQuiz.instance.soundData.get(shuffledQuizIndex.get(indexR++));

        for (Player p : Bukkit.getOnlinePlayers()){
            p.stopAllSounds();
            p.playSound(p.getLocation(), "esq" + currentSound.id + ".sound", SoundCategory.MASTER, 1f, 1f);
        }

        if (bossBar == null) {
            bossBar = Bukkit.getServer().createBossBar(new NamespacedKey(EasySoundQuiz.plugin, "bossBar"), "", BarColor.GREEN, BarStyle.SEGMENTED_20);
            for (Player lap : Bukkit.getOnlinePlayers()) bossBar.addPlayer(lap);
        }

        String bossbarTitle = "\247e정답 키워드 : \247a";

        for (int i = 0; i < currentSound.answerInfo.size(); i++) {
            bossbarTitle += (currentSound.answerInfo.get(i));
            if (i < (currentSound.answerInfo.size() - 1)) bossbarTitle += ", ";
        }

        bossBar.setTitle(bossbarTitle);
        bossBar.setColor(BarColor.GREEN);
        bossBar.setProgress(0);

        quizTask = new BukkitRunnable(){
            int currentTime = 0;
            @Override
            public void run() {
                if (currentTime == timeEnd) TimeOver();
                if ((currentTime % timeHint) == (timeHint - 1)) ShowHint(currentSound);
                bossBar.setProgress(Math.min(currentTime++ / (float)timeEnd, 1));
            }
        }.runTaskTimer(EasySoundQuiz.plugin, 0, 1);
    }

    public void TimeOver() {
        ShowSoundInfo(currentSound);
    }

    public void ShowHint(SoundInfo info) {
        String hint = "\247e힌트 : \247a" + info.getNextHint();

        for (Player p : Bukkit.getOnlinePlayers()) p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(hint));
        Bukkit.broadcastMessage(hint);
    }


    public void ShowSoundInfo(SoundInfo info) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (info.hasImg && useImage.get(p)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " subtitle \"" + info.artist + " - " + info.name + "\"");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " title {\"text\":\"\\uE" + String.format("%03d", info.id) + "\",\"font\":\"esq\"}");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " subtitle \"" + info.artist + " - " + info.name + "\"");
                    } else p.sendTitle(info.name, info.artist, 10, 60, 10);
                }

                Bukkit.broadcastMessage("제목 : " + info.name);
                Bukkit.broadcastMessage("아티스트 : " + info.artist);

                String answerStr = "정답 : ";
                for (int i = 0; i < currentSound.answer.size(); i++) {
                    answerStr += (currentSound.answer.get(i));
                    if (i < (currentSound.answer.size() - 1)) answerStr += ", ";
                }
                Bukkit.broadcastMessage(answerStr);

                bossBar.setProgress(1);
                if (info.winner == null){
                    Bukkit.broadcastMessage("정답자 : \247c없음");
                    bossBar.setColor(BarColor.RED);
                    bossBar.setTitle("\247c타임 아웃!");
                }
                else {
                    Bukkit.broadcastMessage("정답자 : " + info.winner.getName());
                    bossBar.setColor(BarColor.YELLOW);
                    bossBar.setTitle("\2476" + info.winner.getName() + " \247e정답!");
                }

                currentSound.Reset();
                currentSound = null;
            }
        }.runTaskLater(EasySoundQuiz.plugin, 2);

        if (quizTask != null && !quizTask.isCancelled()) quizTask.cancel();
    }
}
