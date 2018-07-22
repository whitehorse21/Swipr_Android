package dk.techtify.swipr.model;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Pavel on 12/26/2016.
 */

public class Faq extends RealmObject {

    @PrimaryKey
    String id;

    String dkQuestion;
    String enQuestion;
    String dkAnswer;
    String enAnswer;

    public Faq() {
    }

    public Faq(String id, Map<String, Object> map) {
        this.id = id;

        HashMap<String, Object> questionMap = map.containsKey("question") ?
                (HashMap<String, Object>) map.get("question") : new HashMap<>();
        this.dkQuestion = questionMap.containsKey("dk") ? questionMap.get("dk").toString() : "";
        this.enQuestion = questionMap.containsKey("en") ? questionMap.get("en").toString() : "";

        HashMap<String, Object> answerMap = map.containsKey("answer") ?
                (HashMap<String, Object>) map.get("answer") : new HashMap<>();
        this.dkAnswer = answerMap.containsKey("dk") ? answerMap.get("dk").toString() : "";
        this.enAnswer = answerMap.containsKey("en") ? answerMap.get("en").toString() : "";
    }

    public String getDkQuestion() {
        return dkQuestion;
    }

    public String getEnQuestion() {
        return enQuestion;
    }

    public String getDkAnswer() {
        return dkAnswer;
    }

    public String getEnAnswer() {
        return enAnswer;
    }
}
