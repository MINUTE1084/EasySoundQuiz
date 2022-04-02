package minute.easysoundquiz;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    public static FileConfiguration config;

    public static int QUIZ_TIME_END = 1200;
    public static int QUIZ_TIME_HINT = 200;
    public static boolean DEFAULT_USE_IMAGE = true;
    public static boolean USE_TIME_BASED_SCORE = true;
    public static int TIME_BASED_SCORE = 5;
    public static int QUIZ_PREPARE_TIME = 400;
    public static float SOUND_VOLUME = 0.5f;
    public static boolean USE_HINT = true;
    public static boolean CAN_USE_LIST = true;
    public static boolean USE_FIREWORK = true;
    public static boolean USE_FIREWORK_ON_RESULT = true;
    public static boolean SHOW_ANSWER_ON_LIST = false;
    public static boolean SHOW_ANSWER_ON_LIST_ADMIN = true;

    public static void Reset(){
        EasySoundQuiz.instance.reloadConfig();
        EasySoundQuiz.instance.saveDefaultConfig();
        config = EasySoundQuiz.instance.getConfig();
        config.options().copyDefaults(true);
        EasySoundQuiz.instance.saveConfig();

        QUIZ_TIME_END = config.getInt("퀴즈 제한 시간") * 20;
        QUIZ_TIME_HINT = config.getInt("힌트 공개 시간") * 20;
        DEFAULT_USE_IMAGE = config.getBoolean("이미지 출력"); //
        USE_TIME_BASED_SCORE = config.getBoolean("시간 기반 득점 사용"); //
        TIME_BASED_SCORE = config.getInt("시간 기반 득점 최고 점수"); //
        QUIZ_PREPARE_TIME = config.getInt("다음 퀴즈 출제 시간") * 20; //
        SOUND_VOLUME = (float)config.getDouble("사운드 볼륨"); //
        USE_HINT = config.getBoolean("힌트 기능 사용"); //
        CAN_USE_LIST = config.getBoolean("유저 사운드 리스트 확인 가능 여부"); //
        USE_FIREWORK = config.getBoolean("정답 시 폭죽 사용"); //
        USE_FIREWORK_ON_RESULT = config.getBoolean("게임 결과 발표 시 폭죽 사용"); //
        SHOW_ANSWER_ON_LIST = config.getBoolean("사운드 리스트 정답 확인 가능 여부 (유저)"); //
        SHOW_ANSWER_ON_LIST_ADMIN = config.getBoolean("사운드 리스트 정답 확인 가능 여부 (관리자)"); //

        if (QUIZ_TIME_END <= 0) {
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c\"퀴즈 제한 시간\" 컨피그 값이 올바르지 않습니다. 기본 값으로 변경합니다.");
            QUIZ_TIME_END = 1200;
        }
        if (QUIZ_TIME_HINT <= 0) {
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c\"힌트 공개 시간\" 컨피그 값이 올바르지 않습니다. 기본 값으로 변경합니다.");
            QUIZ_TIME_HINT = 200;
        }
        if (QUIZ_PREPARE_TIME <= 0) {
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c\"다음 퀴즈 출제 시간\" 컨피그 값이 올바르지 않습니다. 기본 값으로 변경합니다.");
            QUIZ_PREPARE_TIME = 400;
        }
        if (SOUND_VOLUME <= 0) {
            Bukkit.getConsoleSender().sendMessage("\2474[\247cEasySoundQuiz\2474] \247c\"사운드 볼륨\" 컨피그 값이 올바르지 않습니다. 기본 값으로 변경합니다.");
            SOUND_VOLUME = 0.5f;
        }
    }
}
