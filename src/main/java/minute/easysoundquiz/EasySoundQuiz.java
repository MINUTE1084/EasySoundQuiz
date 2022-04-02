package minute.easysoundquiz;

import org.bukkit.Bukkit;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class EasySoundQuiz extends JavaPlugin {
    public static EasySoundQuiz instance;
    public static Plugin plugin;
    public ResourcePackWebServer webServer;
    public GameManager gameManager;

    public ArrayList<SoundInfo> soundData;
    public int hasError = 0;
    public float fileSize = 0;

    public long randomint;
    @Override
    public void onEnable() {
        instance = this;
        plugin = getServer().getPluginManager().getPlugin("EasySoundQuiz");
        ConfigManager.Reset();
        webServer = new ResourcePackWebServer();
        soundData = new ArrayList<>();
        Random random = new Random();
        randomint = random.nextLong();

        getCommand("esq").setExecutor(new CommandManager(this));
        getCommand("esq").setTabCompleter(new TabManager(this));
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        getServer().getPluginManager().registerEvents(new GUIManager(), this);

        try {
            if (LoadSound()) {

                if (!webServer.start()) {
                    Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c웹 서버를 여는데 실패하였습니다. 플러그인을 비활성화 합니다.");
                    getServer().getPluginManager().disablePlugin(this);
                }

                if (hasError > 0)
                    Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c" + hasError + "개의 사운드를 로드하는데 문제가 생겼습니다.");
                Bukkit.getConsoleSender().sendMessage("\2472[\247aEasySoundQuiz\2472] \247av" + instance.getDescription().getVersion() + " 활성화 되었습니다.");
                Bukkit.getConsoleSender().sendMessage("Made by MINUTE.");

                gameManager = new GameManager();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setScoreboard(gameManager.scoreboard);
                    gameManager.useImage.put(p, ConfigManager.DEFAULT_USE_IMAGE);

                    String url = null;
                    try {
                        url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId() + randomint;
                        if (fileSize <= 250) p.setResourcePack(url, null, false);
                        else {
                            p.sendMessage("\247c리소스팩 크기가 250MB를 초과합니다! 다음 링크를 클릭해서 파일을 다운로드 후, 수동 적용해주세요.");
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw @p {\"text\":\"[링크]\",\"underlined\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c리소스팩을 생성하지 못했습니다. 플러그인을 비활성화 합니다.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (Iterator<KeyedBossBar> it = Bukkit.getServer().getBossBars(); it.hasNext(); ) {
            KeyedBossBar bb = it.next();
            bb.removeAll();
            bb.setVisible(false);
            Bukkit.getServer().removeBossBar(bb.getKey());
        }

        for (Player p : Bukkit.getOnlinePlayers()) p.stopAllSounds();

        getServer().getScheduler().cancelTasks(plugin);

        webServer.stopTask();

        File f = new File(webServer.getFileLocation());
        if (f.exists()) f.delete();

        Bukkit.getConsoleSender().sendMessage("\2472[\247aEasySoundQuiz\2472] \247av" + instance.getDescription().getVersion() + " 비활성화 되었습니다.");
        Bukkit.getConsoleSender().sendMessage("Made by MINUTE.");
    }

    public boolean LoadSound() throws IOException {
        File dataFolder = new File(getDataFolder() + "/Sound");
        if (!dataFolder.exists()) dataFolder.mkdir();

        File[] files = dataFolder.listFiles();
        if (files == null || files.length < 1) {
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c사운드 데이터가 없습니다. 플러그인을 비활성화 합니다.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        JSONObject allMusicData = new JSONObject();

        FileOutputStream fos = new FileOutputStream(webServer.getFileLocation());
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File mcmetaFile = new File(getDataFolder() + "/pack.mcmeta");
        if (!mcmetaFile.exists()){
            String mcmeta = """
                {
                   "pack": {
                      "pack_format": 8,
                      "description": "EasySoundQuiz Datapack"
                   }
                }
                """;
            FileWriter fw = new FileWriter(mcmetaFile);
            fw.write(mcmeta);
            fw.flush();
            fw.close();
        }

        addFile(mcmetaFile, "pack.mcmeta", zipOut);

        File thumbnail = new File(getDataFolder() + "/pack.png");
        if (thumbnail.exists()) addFile(mcmetaFile, "pack.png", zipOut);

        JSONArray fontArray = new JSONArray();

        for (File file : files) {
            if (soundData.size() >= 1000) {

                break;
            }
            try {
                if (file.isDirectory()) {
                    String soundName = "";
                    String soundArtist = "";
                    String answer = "";
                    String answerInfo = "";
                    String hint = "";

                    boolean dataOK = false;
                    boolean soundOK = false;
                    boolean imageOK = false;

                    File[] files2 = file.listFiles();
                    for (File file2 : files2) {
                        if (file2.toString().toLowerCase().contains("data.yml")) {
                            Map<String, Object> abilityData = new Yaml().load(new FileReader(file2));
                            soundName = abilityData.get("제목").toString();
                            soundArtist = abilityData.get("아티스트").toString();
                            answer = abilityData.get("정답").toString();
                            answerInfo = abilityData.get("정답 정보").toString();
                            hint = abilityData.get("힌트").toString();

                            dataOK = true;
                        } else if (file2.toString().toLowerCase().contains("sound.ogg")) {
                            addSoundFile(file2, zipOut);

                            JSONObject data = new JSONObject();
                            JSONObject mcName = new JSONObject();
                            mcName.put("name", "esq" + soundData.size() + "/sound");

                            JSONArray array = new JSONArray();
                            array.add(mcName);

                            data.put("sounds", array);
                            allMusicData.put("esq" + soundData.size() + ".sound", data);

                            soundOK = true;
                        } else if (file2.toString().toLowerCase().contains("image.png")){
                            addFile(file2, "assets/minecraft/textures/font/esq/esq" + soundData.size() + ".png", zipOut);

                            JSONObject fontData = new JSONObject();
                            fontData.put("type", "bitmap");
                            fontData.put("file", "minecraft:font/esq/esq" + soundData.size() + ".png");
                            fontData.put("ascent", 20);
                            fontData.put("height", 20);

                            JSONArray chars = new JSONArray();
                            String charData = "\\uE" + String.format("%03d", soundData.size());
                            chars.add(charData);
                            fontData.put("chars", chars);

                            fontArray.add(fontData);

                            imageOK = true;
                        }
                    }

                    if (dataOK && soundOK) {
                        if (!imageOK) Bukkit.getConsoleSender().sendMessage("\2476[\247eEasySoundQuiz\2476] \247e곡 " + soundName + "에 대한 이미지 파일이 존재하지 않습니다.");
                        SoundInfo a = new SoundInfo(soundName, soundArtist, soundData.size(), answer, answerInfo, hint, imageOK);
                        soundData.add(a);
                    }
                }
            } catch (Exception e) {
                hasError++;
                Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c사운드를 로드하는데 문제가 생겼습니다.");
                e.printStackTrace();
            }
        }

        //sound
        File jsonFIle = new File(getDataFolder() + "/sounds.json");
        FileWriter fw = new FileWriter(jsonFIle);
        fw.write(removeEscape(allMusicData.toJSONString()));
        fw.flush();
        fw.close();


        //font
        JSONObject fontJsonData = new JSONObject();
        fontJsonData.put("providers", fontArray);

        File fontJsonFIle = new File(getDataFolder() + "/esq.json");
        fw = new FileWriter(fontJsonFIle);
        fw.write(removeEscape(fontJsonData.toJSONString()));
        fw.flush();
        fw.close();

        addFile(jsonFIle, "assets/minecraft/sounds.json", zipOut);
        addFile(fontJsonFIle, "assets/minecraft/font/esq.json", zipOut);

        zipOut.close();
        fos.close();

        File result = new File(webServer.getFileLocation());
        if (result.exists()){
            fileSize = Files.size(Paths.get(result.getAbsolutePath())) / 1048576f;
        }

        return true;
    }

    private void addSoundFile(File fileToZip, ZipOutputStream zipOut) throws IOException {
        addFile(fileToZip, "assets/minecraft/sounds/esq" + soundData.size() + "/" + fileToZip.getName(), zipOut);
    }

    private void addFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[4096];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    private String removeEscape(String target){
        target = target.replace("\\/", "/");
        target = target.replace("\\\\", "\\");
        return target;
    }

    public void Reset(){
        for (Iterator<KeyedBossBar> it = Bukkit.getServer().getBossBars(); it.hasNext(); ) {
            KeyedBossBar bb = it.next();
            bb.removeAll();
            bb.setVisible(false);
            Bukkit.getServer().removeBossBar(bb.getKey());
        }
        ConfigManager.Reset();
        soundData = new ArrayList<>();
        getServer().getScheduler().cancelTasks(plugin);
        File f = new File(webServer.getFileLocation());
        if (f.exists()) f.delete();

        Random random = new Random();
        randomint = random.nextLong();

        try {
            LoadSound();

            if (hasError > 0)
                Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c" + hasError + "개의 사운드를 로드하는데 문제가 생겼습니다.");
            Bukkit.getConsoleSender().sendMessage("\2472[\247aEasySoundQuiz\2472] \247av" + instance.getDescription().getVersion() + " 리로드 되었습니다.");
            Bukkit.getConsoleSender().sendMessage("Made by MINUTE.");

            gameManager.ResetAll();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.stopAllSounds();

                p.setScoreboard(gameManager.scoreboard);
                gameManager.useImage.put(p, ConfigManager.DEFAULT_USE_IMAGE);

                String url = null;
                try {
                    url = webServer.getWebIp() + p.getUniqueId();
                    if (fileSize <= 250) p.setResourcePack(url, null, false);
                    else {
                        p.sendMessage("\247c리소스팩 크기가 250MB를 초과합니다! 다음 링크를 클릭해서 파일을 다운로드 후, 수동 적용해주세요.");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw @p {\"text\":\"[링크]\",\"underlined\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c리소스팩을 생성하지 못했습니다. 플러그인을 비활성화 합니다.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
