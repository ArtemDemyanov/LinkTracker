package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.state.StateMachine;
import backend.academy.bot.state.TrackState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ListCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListCommandHandler.class);
    private final StateMachine stateMachine;

    public ListCommandHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }

    @Override
    public String handle(Long chatId, String message) {
        logger.info("Handling /list command for chatId {}", chatId);
        stateMachine.setState(chatId, TrackState.AWAITING_TAG_DECISION);
        return "Вывести ссылки по тегам? (да/нет)";
    }
}
