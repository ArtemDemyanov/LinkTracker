package backend.academy.bot.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Класс, содержащий общие сообщения бота. Этот класс предоставляет статические строки, используемые для ответов на
 * различные действия пользователя.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BotMessage {

    /** Сообщение о неизвестной команде. */
    public static final String UNKNOWN_COMMAND = "Неизвестная команда. Введите /help для списка команд.";

    /** Сообщение об успешном добавлении ссылки. */
    public static final String SUCCESS_ADD = "Ссылка успешно добавлена в отслеживание.";

    /** Сообщение об успешном удалении ссылки. */
    public static final String SUCCESS_DELETE = "Ссылка успешно удалена из отслеживания.";

    /** Сообщение с запросом на ввод тегов. */
    public static final String TAGS = "Введите теги (через пробел), чтобы помечать эту ссылку."
            + " Если теги не нужны, просто введите \"Пропустить\".";

    /** Сообщение с запросом на ввод фильтров. */
    public static final String FILTERS = "Настройте фильтры (через пробел в формате \"ключ:значение\")."
            + " Если фильтры не нужны, просто введите \"Пропустить\".";
}
