package minute.easysoundquiz;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class GUIManager implements Listener {
    public static EasySoundQuiz main = EasySoundQuiz.instance;
    private static HashMap<Player, SoundInfo> selectedInfo = new HashMap<>();

    public static SoundInfo GetSelectedInfo(Player player){
        if (!selectedInfo.containsKey(player)) selectedInfo.put(player, null);
        return selectedInfo.get(player);
    }

    public static void RemoveSelectedInfo(Player player){
        if (selectedInfo.containsKey(player)) selectedInfo.remove(player);
    }

    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
        Player player = null;
        ItemStack clicked = null;
        InventoryView inventory = event.getView();

        if (event.getWhoClicked() instanceof Player) player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() != null) clicked = event.getCurrentItem();

        if (player != null && clicked != null) {
            if (inventory.getTitle().contains("\247a곡 리스트")) {
                event.setCancelled(true);

                if (clicked.getType().equals(Material.JUKEBOX)) {
                    String songName = clicked.getItemMeta().getDisplayName();
                    for (SoundInfo si : main.soundData) {
                        if (songName.contains(si.name) && songName.contains(si.artist)) {
                            selectedInfo.replace(player, si);
                            inventory.close();
                            return;
                        }
                    }
                }
                if (clicked.getType().equals(Material.ANVIL)) {
                    inventory.close();
                    OpenSearchInventory(player);
                }
                if (clicked.getType().equals(Material.PLAYER_HEAD)) {
                    SkullMeta headMeta = (SkullMeta) clicked.getItemMeta();
                    PlayerProfile headInfo = headMeta.getOwnerProfile();
                    String searchData = "";
                    if (inventory.getTitle().contains("\2478(검색 : ")) searchData = inventory.getTitle().replace("\247a곡 리스트 \2478(검색 : ", "").replace(")", "");

                    int index = Integer.parseInt(headMeta.getDisplayName().replace("\247aPage ", "").replace("로", "")) - 1;

                    if (headInfo.getName().contains("MHF_Arrow")) {
                        inventory.close();
                        OpenSongList(player, searchData, index);
                    }
                }
            }
        }
    }

    public static void OpenSongList(Player p, String searchData, int index) {
        String title = "\247a곡 리스트";
        ArrayList<SoundInfo> targetSoundData = new ArrayList<>();
        while (searchData.startsWith(" ")) searchData = searchData.substring(1);
        if (searchData.length() > 0) {
            title += " \2478(검색 : " + searchData + ")";
            for (SoundInfo si : main.soundData){
                boolean check = si.artist.contains(searchData) || si.name.contains(searchData);
                for (String answer : si.answer) {
                    if (answer.contains(searchData)) check = true;
                }
                for (String realAnswer : si.realAnswer) {
                    if (realAnswer.contains(searchData.replace(" ", "").toLowerCase())) check = true;
                }
                if (check) targetSoundData.add(si);
            }
        } else {
            targetSoundData = main.soundData;
        }

        boolean checkAnswer = ConfigManager.SHOW_ANSWER_ON_LIST || (p.isOp() && ConfigManager.SHOW_ANSWER_ON_LIST_ADMIN);
        
        Inventory songList = Bukkit.createInventory(p, 54, title);
        for (int i = 0; i < 45 && (i + (45 * index)) < targetSoundData.size(); i++) {
            int infoIndex = i + (45 * index);
            ItemStack item = new ItemStack(Material.JUKEBOX);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\247e" + targetSoundData.get(infoIndex).artist + " \2476- \247e" + targetSoundData.get(infoIndex).name);

            ArrayList<String> lore = new ArrayList<>();
            lore.add("\2477제목 : " + targetSoundData.get(infoIndex).name);
            lore.add("\2477아티스트 : " + targetSoundData.get(infoIndex).artist);
            if (checkAnswer) {
                String answerStr = "\2477정답 : ";
                for (int j = 0; j < targetSoundData.get(infoIndex).answer.size(); j++) {
                    answerStr += (targetSoundData.get(infoIndex).answer.get(j));
                    if (j < (targetSoundData.get(infoIndex).answer.size() - 1)) answerStr += ", ";
                }
                lore.add(answerStr);
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
            songList.setItem(i, item);
        }

        if (index > 0) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setDisplayName("\247aPage " + index + "로");
            meta.setOwnerProfile(Bukkit.createPlayerProfile("MHF_ArrowLeft")); // LEFT
            item.setItemMeta(meta);
            songList.setItem(45, item);
        }

        if ((45 * (index + 1) + 1) <= main.soundData.size()) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setDisplayName("\247aPage " + (index + 2) + "로");
            meta.setOwnerProfile(Bukkit.createPlayerProfile("MHF_ArrowRight"));
            item.setItemMeta(meta);
            songList.setItem(53, item);
        }

        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\247a검색");
        item.setItemMeta(meta);
        songList.setItem(49, item);

        p.openInventory(songList);
    }

    public static void OpenSearchInventory(Player p){
        new AnvilGUI.Builder()
                .onComplete((player, text) ->{
                    OpenSongList(player, text, 0);
                    return AnvilGUI.Response.close();
                })
                .itemLeft(new ItemStack(Material.PAPER))
                .title("\2472곡 검색")
                .text("검색 키워드 입력")
                .plugin(EasySoundQuiz.plugin)
                .open(p);
    }
}
