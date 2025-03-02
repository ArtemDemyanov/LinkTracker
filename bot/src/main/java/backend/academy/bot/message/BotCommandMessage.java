package backend.academy.bot.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Класс, содержащий сообщения для команд бота. Этот класс предоставляет статические строки, используемые для ответов на
 * команды бота.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BotCommandMessage {

    /** Сообщение, отправляемое новому пользователю после регистрации. */
    public static final String WELCOME_NEW_USER =
            "Вы успешно зарегистрировались!\n" + "Нажмите /help, чтобы увидеть список команд";

    /** Сообщение с описанием доступных команд. */
    public static final String HELP_COMMAND = "Доступные команды:\n" + "/track - начать отслеживание ссылки\n"
            + "/untrack - прекратить отслеживание ссылки\n"
            + "/list - показать список отслеживаемых ссылок";

    /** Сообщение с запросом на ввод ссылки. */
    public static final String ENTER_REFERENCE = "Введите ссылку:";

    /** Сообщение с запросом на ввод ссылки для удаления. */
    public static final String ENTER_REFERENCE_FOR_DELETE = "Введите ссылку, которую хотите удалить из отслеживания:";

    /** Сообщение о том, что список отслеживаемых ссылок пуст. */
    public static final String LIST_OF_REF_IS_EMPTY = "Список отслеживаемых ссылок пуст.";

    /** Сообщение с заголовком списка отслеживаемых ссылок. */
    public static final String TRACKED_REF = "Отслеживаемые ссылки:\n";
}
