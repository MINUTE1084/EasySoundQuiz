package minute.easysoundquiz;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

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

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("esqScore", "dummy", "점수");
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);

        Objective obj2 = scoreboard.registerNewObjective("esqScore2", "dummy", "점수");
        obj2.setDisplaySlot(DisplaySlot.SIDEBAR);

        ShuffleIndex();
    }

    private void ShuffleIndex(){
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

    int currentGame = -1;
    int maxGame = 5;
    BukkitTask autoGameTask;
    public void AutoGame(){
        currentGame = maxGame;
        StartRandomQuiz();

        autoGameTask = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (bossBar != null) bossBar.setProgress(Math.min(count++ / (float)400, 1));
                if (currentGame == 0) EndAutoGame();
                else if (currentSound != null) count = 0;
                else if (count == 400 && currentGame > 0) StartRandomQuiz();
            }
        }.runTaskTimer(EasySoundQuiz.plugin, 0, 1);
    }

    public void EndAutoGame() {
        currentGame = -1;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bossBar != null) bossBar.setVisible(false);

                if (autoGameTask != null && !autoGameTask.isCancelled()) autoGameTask.cancel();
                HashMap<Integer, ArrayList<String>> result = new HashMap<>();
                for (String entry : scoreboard.getEntries()) {
                    int score = scoreboard.getObjective("esqScore").getScore(entry).getScore();
                    if (!result.containsKey(score)) result.put(score, new ArrayList<>());
                    result.get(score).add(entry);
                }

                List<Map.Entry<Integer, ArrayList<String>>> data = new ArrayList<>(result.entrySet());
                Collections.sort(data, (o1, o2) -> o2.getKey().compareTo(o1.getKey()));

                Bukkit.broadcastMessage("\2472======================[ \247a결과 \2472]======================");
                String[] place = new String[]{"\247b1위 : ", "\247e2위 : ", "\24773위 : "};
                for (int i = 0; i < 3; i++) {
                    if (i < data.size()) {
                        for (int j = 0; j < data.get(i).getValue().size(); j++) {
                            place[i] += (data.get(i).getValue().get(j));
                            if (j < (data.get(i).getValue().size() - 1)) place[i] += ", ";
                        }
                        Bukkit.broadcastMessage(place[i]);
                    }
                }
                Bukkit.broadcastMessage("\2472===================================================");

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (0 < data.size() && data.get(0).getValue().contains(p.getName())) p.sendTitle("\247b1위!", "\247b축하합니다!", 10, 60, 10);
                    else if (1 < data.size() && data.get(1).getValue().contains(p.getName())) p.sendTitle("\247e2위!", "\247e축하합니다!", 10, 60, 10);
                    else if (2 < data.size() && data.get(2).getValue().contains(p.getName())) p.sendTitle("\24773위!", "\2477축하합니다!", 10, 60, 10);
                    else p.sendTitle("\247c게임 종료!", "\247c모든 문제를 풀었습니다.", 10, 60, 10);
                }
            }
        }.runTaskLater(EasySoundQuiz.plugin, 400);
    }
    
    int indexR = 0;
    public void StartRandomQuiz() {
        if (indexR >= shuffledQuizIndex.size()) {
            ShuffleIndex();
            indexR = 0;
        }

        if (bossBar != null) bossBar.setVisible(false);
        currentSound = null;

        new BukkitRunnable(){
            int count = 3;
            @Override
            public void run() {
                switch (count){
                    case 3 -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle("\247e3", "\247eQuiz " + (maxGame - currentGame + 1) + " / " + maxGame, 10, 20, 10);
                            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 2f);
                        }
                    }
                    case 2 ->{
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle("\24762", "\2476Quiz " + (maxGame - currentGame + 1) + " / " + maxGame, 0, 20, 10);
                            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 2f);
                        }
                    }
                    case 1 ->{
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopAllSounds();
                            p.sendTitle("\247c1", "\247cQuiz " + (maxGame - currentGame + 1) + " / " + maxGame, 0, 20, 10);
                            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 2f);
                        }
                    }
                    case 0 ->{
                        if (bossBar != null) bossBar.setVisible(true);

                        currentSound = EasySoundQuiz.instance.soundData.get(shuffledQuizIndex.get(indexR++));

                        for (Player p : Bukkit.getOnlinePlayers()){
                            p.stopAllSounds();
                            p.sendTitle("\247aPlay!", "\247aQuiz " + (maxGame - currentGame + 1) + " / " + maxGame, 0, 20, 10);
                            p.playSound(p.getLocation(), "esq" + currentSound.id + ".sound", SoundCategory.MASTER, 0.5f, 1f);
                        }

                        if (bossBar == null) {
                            bossBar = Bukkit.getServer().createBossBar(new NamespacedKey(EasySoundQuiz.plugin, "bossBar"), "", BarColor.GREEN, BarStyle.SOLID);
                            for (Player lap : Bukkit.getOnlinePlayers()) bossBar.addPlayer(lap);
                        }

                        bossBar.setColor(BarColor.GREEN);
                        bossBar.setProgress(0);

                        quizTask = new BukkitRunnable(){
                            int currentTime = 0;
                            @Override
                            public void run() {
                                if (currentTime == timeEnd) TimeOver();
                                if ((currentTime % timeHint) == (timeHint - 1)) ShowHint(currentSound);
                                bossBar.setProgress(Math.min(currentTime++ / (float)timeEnd, 1));

                                String bossbarTitle = "\247e정답 키워드 : \247a";

                                for (int i = 0; i < currentSound.answerInfo.size(); i++) {
                                    bossbarTitle += (currentSound.answerInfo.get(i));
                                    if (i < (currentSound.answerInfo.size() - 1)) bossbarTitle += ", ";
                                }

                                if (useTimeBasedScore) bossbarTitle += " \2477(" + (int)Math.ceil(5 * (1 - bossBar.getProgress())) + "점)";
                                bossBar.setTitle(bossbarTitle);
                            }
                        }.runTaskTimer(EasySoundQuiz.plugin, 0, 1);
                    }
                }
                count--;
            }
        }.runTaskTimer(EasySoundQuiz.plugin, 0, 20);
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
        final int score = useTimeBasedScore ? (int)Math.ceil(5 * (1 - bossBar.getProgress())) : 1;
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "gamerule sendCommandFeedback false");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (info.hasImg && useImage.get(p)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " subtitle [\"\",{\"text\":\"" + info.artist + "\",\"color\":\"yellow\"},{\"text\":\" - \",\"color\":\"gold\"},{\"text\":\"" + info.name + "\",\"color\":\"yellow\"}]");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " title {\"text\":\"\\uE" + String.format("%03d", info.id) + "\",\"font\":\"esq\"}");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " times 10 60 10");
                    } else p.sendTitle("\2476" + info.name, "\247e" + info.artist, 10, 60, 10);
                }

                Bukkit.broadcastMessage("\2476======================[ \247e정보 \2476]======================");
                Bukkit.broadcastMessage("\247e제목 : \247a" + info.name);
                Bukkit.broadcastMessage("\247e아티스트 : \247a" + info.artist);
                String answerStr = "\247e정답 : \247a";
                for (int i = 0; i < currentSound.answer.size(); i++) {
                    answerStr += (currentSound.answer.get(i));
                    if (i < (currentSound.answer.size() - 1)) answerStr += "\247e, \247a";
                }
                Bukkit.broadcastMessage(answerStr);

                if (info.winner == null){
                    Bukkit.broadcastMessage("\2476정답자 : \247c없음");
                    bossBar.setColor(BarColor.RED);
                    bossBar.setTitle("\247c타임 아웃!");
                }
                else {
                    Bukkit.broadcastMessage("\2476정답자 : \247e" + info.winner.getName() + " \2477(" + score + "점)");
                    bossBar.setColor(BarColor.YELLOW);
                    bossBar.setTitle("\2476" + info.winner.getName() + " \247e정답!");
                    summonFirework(info.winner);
                }

                Bukkit.broadcastMessage("\2476===================================================");

                bossBar.setProgress(1);
                currentSound.Reset();
                currentSound = null;

                currentGame--;
            }
        }.runTaskLater(EasySoundQuiz.plugin, 2);

        if (quizTask != null && !quizTask.isCancelled()) quizTask.cancel();
    }

    public void summonFirework(Player player){
        final Firework f = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fm = f.getFireworkMeta();

        fm.addEffect(FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.STAR)
                .with(FireworkEffect.Type.BALL)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.AQUA)
                .withColor(Color.YELLOW)
                .withColor(Color.RED)
                .withColor(Color.WHITE)
                .build());

        fm.setPower(0);
        f.setFireworkMeta(fm);
    }
}
