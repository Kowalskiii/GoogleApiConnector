package org.example.constans;

public enum BotMessageEnum {
    START_MESSAGE ("Приветствую, я помогу тебе найти что-нибудь в Google. Что будем искать?"),
    MORE_MESSAGE("Посмотреть другие варианты \uD83D\uDC47"),
    NOT_RESULT_MESSAGE("По текущему запросу больше ничего не найдено. Введите новый запрос."),
    ERROR_MESSAGE("Что-то пошло не по плану\uD83E\uDD14 \nВведите новый поисковый запрос."),
    NOT_STRING_MESSAGE("К сожалению, я умею обрабатывать только текстовые запросы☹\uFE0F \nВведите корректный запрос."),

    NOT_FOUND_MASSAGE("Ничего не найдено☹\uFE0F \nПопробуйте другой запрос.");
    private final String message;

    BotMessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
