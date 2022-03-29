package minute.easysoundquiz;

import java.util.ArrayList;
import java.util.List;

public class SoundInfo {
    String name;
    String artist;
    int id;
    boolean hasImg = false;
    ArrayList<String> answer = new ArrayList<>();
    ArrayList<String> answerInfo = new ArrayList<>();
    ArrayList<String> hint = new ArrayList<>();

    private int currentHint = 0;

    public SoundInfo(String _name, String _artist, int _id, String _answer, String _answerInfo, String _hint, boolean _hasImg) {
        name = _name;
        artist = _artist;
        id = _id;
        hasImg = _hasImg;

        String[] answerData = _answer.split(",");
        for (int i = 0; i < answerData.length; i++)
            while (answerData[i].startsWith(" "))
                answerData[i] = answerData[i].substring(1);

        answer.addAll(List.of(answerData));

        String[] answerInfoData = _answerInfo.split(",");
        for (int i = 0; i < answerData.length; i++)
            while (answerInfoData[i].startsWith(" "))
                answerInfoData[i] = answerInfoData[i].substring(1);

        answerInfo.addAll(List.of(answerInfoData));

        String[] hintData = _hint.split(",");
        for (int i = 0; i < answerData.length; i++)
            while (hintData[i].startsWith(" "))
                hintData[i] = hintData[i].substring(1);

        hint.addAll(List.of(hintData));
    }

    public String getNextHint() { return hint.size() < currentHint ? hint.get(currentHint++) : ""; }

    public void Reset() {
        currentHint = 0;
    }
}