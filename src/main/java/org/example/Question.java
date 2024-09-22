package org.example;


import java.util.*;

public class Question {
    private String question;
    private String[] options;
    private int[] answers;
    private int totalAnswers;

    public Question(String question, String[] options) {
        this.question = question;
        this.options = options;
        this.answers = new int[options.length];
        this.totalAnswers = 0;
    }

    public void answer(int answer) {
        this.answers[answer-1]++;
        this.totalAnswers++;
    }

    @Override
    public String toString(){
        String output = question + "\n";
        for(int i = 0; i < options.length; i++){
            output += "\n" + (i+1) + ". " + options[i];
        }
        return output;
    }

    public String getQuestionResults(){
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < answers.length; i++) {
            map.put(options[i], answers[i]);
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        String output = question + "\n\n";
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            output += entry.getKey() + " - " + (float)((entry.getValue() / totalAnswers)*100) + "%\n";
        }
        return output;

    }
}
