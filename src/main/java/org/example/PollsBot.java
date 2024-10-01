package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class PollsBot extends TelegramLongPollingBot {
    private List<Long> chatIds = new ArrayList<>();
    private Poll activePoll = null;
    private final String WELCOME_MESSAGE = "Welcome!\nCommands List:" +
            "\n/create_poll\n/submit_poll\n/cancel\n/add\n/answer";

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String msg = update.getMessage().getText();

        if (activePoll != null)
            if (activePoll.getIsActive())
                if ((System.currentTimeMillis() - activePoll.getSubmittionTime()) >= 5 * 60 * 1000) //5 minutes passed
                    endPoll();

        if (!chatIds.contains(chatId) && (msg.equals("Hi") || msg.equals("היי"))) {
            addNewMember(chatId);
            return;
        }
        if (!chatIds.contains(chatId))
            return;
        if (msg.equals("/create_poll")) {
            createPoll(chatId);
        }
        if (msg.equals("/cancel")) {
            cancelPoll(chatId);
        }
        if (msg.startsWith("/add")) {
            addNewQuestion(chatId, msg);
        }
        if (msg.equals("/submit_poll")) {
            submitPoll(chatId);
        }
        if (msg.startsWith("/answer")) {
            answerPoll(chatId, msg);
        }


    }

    @Override
    public String getBotUsername() {
        return "polls_bot";
    }

    @Override
    public String getBotToken() {
        return "REDACTED";
    }

    public void addNewMember(Long chatId) {
        try {
            chatIds.add(chatId);
            execute(new SendMessage(String.valueOf(chatId), WELCOME_MESSAGE));
            for (Long id : chatIds) {
                if (id != chatId) {
                    execute(new SendMessage(String.valueOf(id), "A new member has joined!\nTotal members: " + chatIds.size()));
                }
            }
        }
        catch (TelegramApiException e) {}
    }

    public void createPoll(Long chatId) {
        try {
            if (chatIds.size() < 3){
                execute(new SendMessage(String.valueOf(chatId), "need at least 3 members!"));
                return;
            }
            if (activePoll == null) {
                activePoll = new Poll(chatId);
                execute(new SendMessage(String.valueOf(chatId), "Creating a new poll!\n" +
                        "To add a new question type /add (question):(option1),(option2),(option3)\n" +
                        "You can add up to 3 questions with up to 4 options.\n" +
                        "Once you are done adding questions type /submit_poll"));
            }
            else
                execute(new SendMessage(String.valueOf(chatId), "There is an ongoing poll!"));
        }
        catch (TelegramApiException e) {}
    }

    public void addNewQuestion(Long chatId, String msg) {
        try {
            if (activePoll == null) {
                execute(new SendMessage(String.valueOf(chatId), "no ongoing poll, please use /create_poll first"));
                return;
            }
            if (activePoll.getPollOwnerId() != chatId) {
                execute(new SendMessage(String.valueOf(chatId), "You are not the owner of this poll!"));
                return;
            }
            String questionInfo = msg.split(" ", 2)[1];
            String questionContent = questionInfo.split(":")[0];
            String[] options = questionInfo.split(":")[1].split(",");

            if (options.length < 2 || options.length > 4){
                execute(new SendMessage(String.valueOf(chatId), "Must have between 2 and 4 options"));
                return;
            }
            Question question = new Question(questionContent, options);
            if (activePoll.addQuestion(question)) {
                execute(new SendMessage(String.valueOf(chatId), "New question added!"));
            }
            else
                execute(new SendMessage(String.valueOf(chatId), "Max amount of questions reached!"));

        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void submitPoll(Long chatId){
        try {
            if (activePoll == null) {
                execute(new SendMessage(String.valueOf(chatId), "no ongoing poll, please use /create_poll first"));
                return;
            }
            if (activePoll.getPollOwnerId() != chatId) {
                execute(new SendMessage(String.valueOf(chatId), "You are not the owner of this poll!"));
                return;
            }

            if (activePoll.getIsActive()){
                execute(new SendMessage(String.valueOf(chatId), "poll was already submitted"));
            }

            else {
                execute(new SendMessage(String.valueOf(chatId), "Your poll has been submitted!"));
                activePoll.setActive();
                sendPoll();
            }

        }
        catch (TelegramApiException e) {}
    }

    public void sendPoll(){
        try {
            for (Long id : chatIds) {
                execute(new SendMessage(String.valueOf(id), activePoll.toString() + "\n\n" +
                        "/answer (answer1),(answer2),..."));

            }
        }
        catch (TelegramApiException e) {}
    }

    public void answerPoll(Long chatId, String msg){
        try {
            if (activePoll == null) {
                execute(new SendMessage(String.valueOf(chatId), "no ongoing poll, please use /create_poll first"));
                return;
            }
            if (!activePoll.getIsActive()){
                execute(new SendMessage(String.valueOf(chatId), "the poll hasn't been submitted yet, please wait"));
                return;
            }
            if (activePoll.hasAnswered(chatId))
            {
                execute(new SendMessage(String.valueOf(chatId), "you've already responded!"));
                return;
            }
            String[] answers = msg.split(" ", 2)[1].split(",");
            if (answers.length != activePoll.getQuestions().size())
            {
                execute(new SendMessage(String.valueOf(chatId), "error, make sure you've answered every question"));
                return;
            }
            activePoll.submitResponse(chatId, answers);
            execute(new SendMessage(String.valueOf(chatId), "answers submitted!"));
            if (activePoll.getTotalResponses() == chatIds.size())
                endPoll();

        }
        catch (TelegramApiException e) {}
    }

    public void cancelPoll(Long chatId){
        try {
            if (activePoll == null) {
                execute(new SendMessage(String.valueOf(chatId), "no ongoing poll, please use /create_poll first"));
                return;
            }
            if (activePoll.getPollOwnerId() != chatId) {
                execute(new SendMessage(String.valueOf(chatId), "You are not the owner of this poll!"));
                return;
            }
            execute(new SendMessage(String.valueOf(chatId), "your poll has been cancelled!"));
            this.activePoll = null;
        }
        catch (TelegramApiException e) {}

    }

    public void endPoll(){
        try {
            execute(new SendMessage(String.valueOf(activePoll.getPollOwnerId()), "Your poll has ended! Results:\n" +
                    activePoll.getPollResults()));
            activePoll = null;
        }
        catch (TelegramApiException e) {}
    }
}
