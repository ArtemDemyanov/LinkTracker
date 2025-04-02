package backend.academy.bot.commands.impl;

import static backend.academy.bot.message.BotCommandMessage.ENTER_REFERENCE_FOR_DELETE;

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

    /** @param stateMachine Машина состояний для управления состоянием пользователя. */
    public UntrackCommandHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * @param chatId Уникальный идентификатор чата, в котором была отправлена команда.
     * @param message Текст сообщения, содержащий команду /untrack.
     * @return Сообщение с запросом на ввод ссылки для удаления.
     */
    @Override
    public String handle(Long chatId, String message) {
        logger.atInfo()
                .setMessage("Handling /untrack command")
                .addKeyValue("chatId", chatId)
                .log();

        stateMachine.setState(chatId, TrackState.AWAITING_UNTRACK_LINK);
        return ENTER_REFERENCE_FOR_DELETE;
    }
}
