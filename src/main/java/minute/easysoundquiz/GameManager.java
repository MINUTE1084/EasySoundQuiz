package minute.easysoundquiz;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Random;

public class GameManager {
    public Scoreboard scoreboard;
    public boolean isQuizMode = false;
    
    public ArrayList<Integer> shuffledQuizIndex;
    public SoundInfo currentSound;

    public KeyedBossBar bossBar = null;

    public int timeEnd;
    public int timeEnd;


    private BukkitTask quizTask;

    public GameManager() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        shuffledQuizIndex = new ArrayList<>();
        
        for (int i = 0; i < EasySoundQuiz.instance.soundData.size(); i++) shuffledQuizIndex.add(i);

        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            int randomIndex = random.nextInt(0, shuffledQuizIndex.size());
            final int temp = shuffledQuizIndex.get(0);
            shuffledQuizIndex.set(0, shuffledQuizIndex.get(randomIndex));
            shuffledQuizIndex.set(randomIndex, temp);
        }
    }
    
    public SoundInfo getCurrentSound() {
        return currentSound;
    }
    
    int indexR = 0;
    public void StartRandomQuiz() {
        currentSound = EasySoundQuiz.instance.soundData.get(shuffledQuizIndex.get(indexR++));
        
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

        quizTask = new BukkitRunnable(){
            int currentTime = 0;
            @Override
            public void run() {
                if (currentTime == timeEnd) TimeOver();
                bossBar.setProgress(currentTime++ / (float)timeEnd);
            }
        }.runTaskTimer(EasySoundQuiz.plugin, 0, timeEnd);
    }

    public void TimeOver() {
        currentSound.Reset();

    }

    public void ShowSoundInfo(SoundInfo info) {
        for (Player p : Bukkit.getOnlinePlayers()){
            
        }
    }
}
