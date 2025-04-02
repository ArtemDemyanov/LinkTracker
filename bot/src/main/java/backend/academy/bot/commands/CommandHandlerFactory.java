package backend.academy.bot.commands;

import backend.academy.bot.commands.impl.HelpCommandHandler;
import backend.academy.bot.commands.impl.ListCommandHandler;
import backend.academy.bot.commands.impl.StartCommandHandler;
import backend.academy.bot.commands.impl.TrackCommandHandler;
import backend.academy.bot.commands.impl.UntrackCommandHandler;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerFactory.class);
    private final Map<String, BotCommandHandler> handlers = new HashMap<>();

    /**
     * Фабрика для создания и управления обработчиками команд бота.
     *
     * @param startCommandHandler Обработчик команды /start.
     * @param helpCommandHandler Обработчик команды /help.
     * @param trackCommandHandler Обработчик команды /track.
     * @param untrackCommandHandler Обработчик команды /untrack.
     * @param listCommandHandler Обработчик команды /list.
     */
    public CommandHandlerFactory(
            StartCommandHandler startCommandHandler,
            HelpCommandHandler helpCommandHandler,
            TrackCommandHandler trackCommandHandler,
            UntrackCommandHandler untrackCommandHandler,
            ListCommandHandler listCommandHandler) {
        handlers.put("/start", startCommandHandler);
        handlers.put("/help", helpCommandHandler);
        handlers.put("/track", trackCommandHandler);
        handlers.put("/untrack", untrackCommandHandler);
        handlers.put("/list", listCommandHandler);

        logger.atInfo()
                .setMessage("Command handlers initialized")
                .addKeyValue("handlers", handlers.keySet())
                .log();
    }

    /**
     * Возвращает обработчик для указанной команды.
     *
     * @param command Название команды (например, "/start").
     * @return Обработчик команды или null, если обработчик не найден.
     */
    public BotCommandHandler getHandler(String command) {
        BotCommandHandler handler = handlers.get(command);
        logger.atDebug()
                .setMessage("Retrieved handler for command")
                .addKeyValue("command", command)
                .addKeyValue("handler", handler != null ? handler.getClass().getSimpleName() : "null")
                .log();
        return handler;
    }
}
