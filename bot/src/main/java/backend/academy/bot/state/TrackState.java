package backend.academy.bot.state;

public enum TrackState {
    /** Состояние бездействия (ожидание команды). */
    IDLE,

    /** Состояние ожидания ссылки. */
    AWAITING_LINK,

    /** Состояние ожидания тегов. */
    AWAITING_TAGS,

    /** Состояние ожидания фильтров. */
    AWAITING_FILTERS,

    /** Состояние ожидания ссылки для удаления. */
    AWAITING_UNTRACK_LINK,

    AWAITING_TAG_DECISION,

    AWAITING_TAGS_INPUT
}
