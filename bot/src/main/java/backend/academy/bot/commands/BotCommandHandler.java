package backend.academy.bot.commands;

public interface BotCommandHandler {

    /**
     * Интерфейс для обработки команд бота.
     *
     * @param chatId Уникальный идентификатор чата, в котором была отправлена команда.
     * @param message Текст сообщения, содержащий команду.
     * @return Ответное сообщение, которое будет отправлено пользователю.
     */
    String handle(Long chatId, String message);
}
