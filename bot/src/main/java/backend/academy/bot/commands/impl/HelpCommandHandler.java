package backend.academy.bot.commands.impl;

import static backend.academy.bot.message.BotCommandMessage.HELP_COMMAND;

import backend.academy.bot.commands.BotCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelpCommandHandler.class);

    /**
     * @param chatId Уникальный идентификатор чата, в котором была отправлена команда.
     * @param message Текст сообщения, содержащий команду /help.
     * @return Строка с описанием доступных команд.
     */
    @Override
    public String handle(Long chatId, String message) {
        logger.atInfo()
                .setMessage("Handling /help command")
                .addKeyValue("chatId", chatId)
                .log();

        return HELP_COMMAND;
    }
}
