package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.constans.BotMessageEnum;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchBot extends TelegramLongPollingBot {

    Map<Long,SerchRequest> requestsPool = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "CustomSearchBot";
    }

    @Override
    public String getBotToken() {
        return "6926613773:AAEcwarVoy_RdVeJhg2YN9bUfIENaj54f2k";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        User user = new User();
        String msgText = "";
        if (msg != null) {
            msgText = msg.getText() != null ? msg.getText() : msgText;
            user = msg.getFrom() != null ? msg.getFrom() : user;
        }
        else if (update.getCallbackQuery() != null) {
            msgText = update.getCallbackQuery().getData() != null ? update.getCallbackQuery().getData() : msgText;
            user = update.getCallbackQuery().getFrom() != null ? update.getCallbackQuery().getFrom() : user;
        }

        switch (msgText) {
            case "next" -> onNextResult(user);
            case "/start" -> sendText(user, BotMessageEnum.START_MESSAGE.getMessage());
            default  -> onNewSearch(msgText, user);
        }
    }

    public void onNewSearch(String search, User user) {
        var itemList = new SearchItemList();

        if (search != null && !search.isEmpty()) {
            var responseBody = HttpHelper.GetGoogleResults(search.replaceAll("\\s+", ""));
            try {
                itemList = getSearchResults(responseBody);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (itemList != null && itemList.getItems() != null) {
                SerchRequest serchItem = new SerchRequest();
                serchItem.lastIndex = 0;
                serchItem.lastSearch = search;
                serchItem.lastSearchResult = itemList;

                var curItem = itemList.getItems().get(0);
                var result = curItem.getTitle() + "\n" + curItem.getSnippet() + "\n" + curItem.getLink();
                sendText(user, result);
                serchItem.lastIndex++;

                requestsPool.put(user.getId(), serchItem);

                sendKeyboard("next", user);
            }
            else {
                sendText(user, BotMessageEnum.NOT_FOUND_MASSAGE.getMessage());
            }
        }
        else {
            sendText(user, BotMessageEnum.NOT_STRING_MESSAGE.getMessage());
        }
    }

    public void onNextResult(User user) {
        var userCurSerch = requestsPool.get(user.getId());

        if (userCurSerch == null) {
            sendText(user, BotMessageEnum.NOT_RESULT_MESSAGE.getMessage());
            return;
        }

        if (userCurSerch.lastSearchResult == null || userCurSerch.lastSearchResult.getItems() == null || userCurSerch.lastSearch.isEmpty()) {
            sendText(user, BotMessageEnum.NOT_RESULT_MESSAGE.getMessage());
            return;
        }

        if (userCurSerch.lastIndex < userCurSerch.lastSearchResult.getItems().size()) {
            var curItem = userCurSerch.lastSearchResult.getItems().get(userCurSerch.lastIndex);
            var result = curItem.getTitle() + "\n" + curItem.getSnippet() + "\n" + curItem.getLink();
            sendText(user, result);
            userCurSerch.lastIndex++;
            sendKeyboard("next", user);
        }
        else {
            sendText(user, BotMessageEnum.NOT_RESULT_MESSAGE.getMessage());
            requestsPool.remove(user.getId());
        }
    }

    public void sendText(User user, String message){
        SendMessage sm = SendMessage.builder()
                .chatId(user.getId().toString())
                .text(message).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchItemList getSearchResults(String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var pojoResponse = objectMapper.readValue(body, SearchItemList.class);

        return pojoResponse;
    }

    private void sendKeyboard(String option, User user) {

        SendMessage sm = SendMessage.builder().chatId(user.getId().toString())
                .parseMode("HTML").text(BotMessageEnum.MORE_MESSAGE.getMessage())
                .replyMarkup(Buttons.inSearchKeyboard).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Buttons {
        static InlineKeyboardButton next = InlineKeyboardButton.builder()
                .text("Далее").callbackData("next")
                .build();


        static InlineKeyboardMarkup inSearchKeyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(next))
                .build();
    }

    private class SerchRequest {
        String lastSearch = "";
        SearchItemList lastSearchResult = new SearchItemList();
        Integer lastIndex = 0;
    }

}
