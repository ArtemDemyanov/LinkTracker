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

    /** @param stateMachine Экземпляр машины состояний, используемый для управления состоянием чатов. */
    public TrackCommandHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * @param chatId Идентификатор чата, в котором была вызвана команда.
     * @param message Текст сообщения, содержащего команду (не используется в данной реализации).
     * @return Сообщение пользователю с запросом на ввод ссылки для отслеживания.
     */
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
