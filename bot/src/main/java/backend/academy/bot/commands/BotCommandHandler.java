package backend.academy.bot.commands;

public interface BotCommandHandler {
    String command();

    String description();

    String handle(Long chatId, String message);
}
