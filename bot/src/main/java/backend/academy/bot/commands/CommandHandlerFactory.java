package backend.academy.bot.commands;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerFactory.class);
    private final Map<String, BotCommandHandler> handlers = new HashMap<>();
    private final List<BotCommandHandler> commandHandlers;

    public CommandHandlerFactory(List<BotCommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    @PostConstruct
    public void init() {
        for (BotCommandHandler handler : commandHandlers) {
            handlers.put(handler.command(), handler);
        }
        logger.info("Command handlers initialized: {}", handlers.keySet());
    }

    public BotCommandHandler getHandler(String command) {
        return handlers.get(command);
    }

    public String availableCommands() {
        return handlers.values().stream()
                .map(h -> h.command() + " - " + h.description())
                .sorted()
                .reduce("Доступные команды:", (a, b) -> a + "\n" + b);
    }
}
