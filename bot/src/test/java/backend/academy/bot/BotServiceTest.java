package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.CommandHandlerFactory;
import backend.academy.bot.commands.impl.*;
import backend.academy.bot.service.BotService;
import backend.academy.bot.state.StateMachine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BotServiceTest {

    @Test
    void testHandleUnknownCommand() {
        // Создаем моки для всех зависимостей
        StateMachine stateMachine = new StateMachine(); // Используем реальный объект StateMachine
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);

        // Создаем моки для конкретных реализаций обработчиков команд
        StartCommandHandler startCommandHandler = Mockito.mock(StartCommandHandler.class);
        HelpCommandHandler helpCommandHandler = Mockito.mock(HelpCommandHandler.class);
        TrackCommandHandler trackCommandHandler = Mockito.mock(TrackCommandHandler.class);
        UntrackCommandHandler untrackCommandHandler = Mockito.mock(UntrackCommandHandler.class);
        ListCommandHandler listCommandHandler = Mockito.mock(ListCommandHandler.class);

        // Создаем CommandHandlerFactory с моками
        CommandHandlerFactory commandHandlerFactory = new CommandHandlerFactory(
                startCommandHandler,
                helpCommandHandler,
                trackCommandHandler,
                untrackCommandHandler,
                listCommandHandler);

        // Создаем BotService с моками
        BotService botService = new BotService(commandHandlerFactory, stateMachine, scrapperClient);

        // Вызываем метод и проверяем результат
        String response = botService.handleMessage(12345L, "/unknown");
        assertEquals("Неизвестная команда. Введите /help для списка команд.", response);
    }
}
