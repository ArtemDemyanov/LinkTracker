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

    /** @param stateMachine Экземпляр машины состояний, используемый для управления состоянием чатов. */
    public ListCommandHandler(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * @param chatId Идентификатор чата, в котором была вызвана команда.
     * @param message Текст сообщения, содержащего команду (не используется в данной реализации).
     * @return Сообщение пользователю с запросом на вывод ссылок по тегам.
     */
    @Override
    public String handle(Long chatId, String message) {
        logger.atInfo()
                .setMessage("Обработка команды /list")
                .addKeyValue("chatId", chatId)
                .log();

        stateMachine.setState(chatId, TrackState.AWAITING_TAG_DECISION);
        return "Вывести ссылки по тегам? (да/нет)";
    }
}
