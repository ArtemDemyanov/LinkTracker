package backend.academy.bot.commands.impl;

import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.state.StateMachine;
import backend.academy.bot.state.TrackState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(UntrackCommandHandler.class);
    private final StateMachine stateMachine;

    public UntrackCommandHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Прекратить отслеживание ссылки";
    }

    @Override
    public String handle(Long chatId, String message) {
        logger.info("Handling /untrack command for chatId {}", chatId);
        stateMachine.setState(chatId, TrackState.AWAITING_UNTRACK_LINK);
        return "Введите ссылку, которую хотите удалить из отслеживания:";
    }
}
