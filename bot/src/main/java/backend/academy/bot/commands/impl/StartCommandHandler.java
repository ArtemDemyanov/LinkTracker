package backend.academy.bot.commands.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.BotCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartCommandHandler.class);
    private final ScrapperClient scrapperClient;

    public StartCommandHandler(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Регистрация пользователя";
    }

    @Override
    public String handle(Long chatId, String message) {
        logger.info("Handling /start command for chatId {}", chatId);
        scrapperClient.registerChat(chatId).subscribe();
        return "Вы успешно зарегистрировались!\nНажмите /help, чтобы увидеть список команд";
    }
}
