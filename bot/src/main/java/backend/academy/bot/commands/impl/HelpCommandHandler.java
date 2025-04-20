package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.commands.CommandHandlerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements BotCommandHandler {

    private final ObjectProvider<CommandHandlerFactory> commandHandlerFactoryProvider;

    public HelpCommandHandler(ObjectProvider<CommandHandlerFactory> commandHandlerFactoryProvider) {
        this.commandHandlerFactoryProvider = commandHandlerFactoryProvider;
    }

    @Override
    public String handle(Long chatId, String message) {
        return commandHandlerFactoryProvider.getObject().availableCommands();
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Показать список команд";
    }
}
