package backend.academy.scrapper.controller.response;

import java.util.List;

/**
 * Ответ с информацией об ошибке.
 *
 * @param description Описание ошибки.
 * @param code Код ошибки.
 * @param exceptionName Название исключения.
 * @param exceptionMessage Сообщение исключения.
 * @param stacktrace Стек вызовов, связанный с ошибкой.
 */
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
