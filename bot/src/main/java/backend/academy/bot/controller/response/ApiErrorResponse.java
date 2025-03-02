package backend.academy.bot.controller.response;

import java.util.List;

/**
 * Представляет объект ответа с информацией об ошибке. Этот класс используется для передачи деталей ошибки в API.
 *
 * @param description Описание ошибки.
 * @param code Код ошибки.
 * @param exceptionName Название исключения.
 * @param exceptionMessage Сообщение исключения.
 * @param stacktrace Стек вызовов, связанный с ошибкой.
 */
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
