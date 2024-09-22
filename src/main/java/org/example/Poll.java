package org.example;

import java.util.ArrayList;
import java.util.List;

public class Poll {
    private long pollOwnerId;
    private List<Question> questions;
    private List<Long> responded;
    private boolean isActive;
    private long submittionTime;

    public Poll(long pollOwnerId) {
        this.pollOwnerId = pollOwnerId;
        questions = new ArrayList<Question>();
        responded = new ArrayList<>();
        isActive = false;
    }

    public long getPollOwnerId() {
        return pollOwnerId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public boolean addQuestion(Question question) {
        if (questions.size() < 3) {
            questions.add(question);
            return true;
        }
        return false;
    }

    public void submitResponse(Long chatId, String[] answers){
        for (int i = 0; i < answers.length; i++) {
            questions.get(i).answer(Integer.parseInt(answers[i]));
        }
        responded.add(chatId);
    }

    public boolean hasAnswered(Long chatId){
        if (responded.contains(chatId)) {
            return true;
        }
        return false;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public long getSubmittionTime() {
        return submittionTime;
    }

    public void setActive() {
        submittionTime = System.currentTimeMillis();
        isActive = true;
    }

    public int getTotalResponses(){
        return responded.size();
    }

    @Override
    public String toString() {
        String output = "";
        int c = 1;
        for (Question question : questions) {
            output += c + ". " + question.toString() + "\n\n";
            c++;
        }
        return output;
    }

    public String getPollResults() {
        String output = "";
        for (Question question : questions) {
            output += question.getQuestionResults() + "\n\n";
        }
        return output;
    }
}
