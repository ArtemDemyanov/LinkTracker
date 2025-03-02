package backend.academy.bot.commands.impl;

import static backend.academy.bot.message.BotCommandMessage.ENTER_REFERENCE;

import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.state.StateMachine;
import backend.academy.bot.state.TrackState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TrackCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrackCommandHandler.class);
    private final StateMachine stateMachine;

    public TrackCommandHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public String handle(Long chatId, String message) {
        logger.atInfo()
                .setMessage("Handling /track command")
                .addKeyValue("chatId", chatId)
                .log();

        stateMachine.setState(chatId, TrackState.AWAITING_LINK);
        return ENTER_REFERENCE;
    }
}
