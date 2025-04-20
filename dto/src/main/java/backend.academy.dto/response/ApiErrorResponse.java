package backend.academy.dto.response;

import java.util.List;

/**
 * DTO для представления информации об ошибке API.
 *
 * @param description Описание ошибки.
 * @param code Код ошибки.
 * @param exceptionName Название исключения.
 * @param exceptionMessage Сообщение исключения.
 * @param stacktrace Стек вызовов, связанный с ошибкой.
 */
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
