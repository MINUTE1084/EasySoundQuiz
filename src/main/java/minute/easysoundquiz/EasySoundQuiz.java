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

    @Override
    public void onEnable() {
        instance = this;
        plugin = getServer().getPluginManager().getPlugin("EasySoundQuiz");
        webServer = new ResourcePackWebServer();
        soundData = new ArrayList<>();

        getCommand("esq").setExecutor(new CommandManager(this));
        getServer().getPluginManager().registerEvents(new EventManager(), this);

        try {
            if (webServer.start()) LoadSound();
            else {
                Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c웹 서버를 여는데 실패하였습니다. 플러그인을 비활성화 합니다.");
                getServer().getPluginManager().disablePlugin(this);
            }
        } catch (Exception e){
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c리소스팩을 생성하지 못했습니다. 플러그인을 비활성화 합니다.");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (hasError > 0) Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c" + hasError + "개의 사운드를 로드하는데 문제가 생겼습니다.");
        Bukkit.getConsoleSender().sendMessage("\2472[\247aEasySoundQuiz\2472] \247av" + instance.getDescription().getVersion() + " 활성화 되었습니다.");
        Bukkit.getConsoleSender().sendMessage("Made by MINUTE.");

        gameManager = new GameManager();
        Random random = new Random();
        for (Player p : Bukkit.getOnlinePlayers()){
            p.setScoreboard(gameManager.scoreboard);
            gameManager.useImage.put(p, gameManager.defaultUseImage);

            String url = null;
            try {
                url = EasySoundQuiz.instance.webServer.getWebIp() + p.getUniqueId();
                p.setResourcePack(url, null, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        Bukkit.getConsoleSender().sendMessage("\2472[\247aEasySoundQuiz\2472] \247av" + instance.getDescription().getVersion() + " 비활성화 되었습니다.");
        Bukkit.getConsoleSender().sendMessage("Made by MINUTE.");
    }

    public void LoadSound() throws IOException {
        File dataFolder = new File(getDataFolder() + "/Sound");
        File[] files = dataFolder.listFiles();
        JSONObject allMusicData = new JSONObject();

        FileOutputStream fos = new FileOutputStream(getDataFolder() + "/EasySoundQuiz.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File mcmetaFile = new File(getDataFolder() + "/pack.mcmeta");
        if (!mcmetaFile.exists()){
            String mcmeta = """
                {
                   "pack": {
                      "pack_format": 7,
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
}
