package backend.academy.scrapper.domain.jdbc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LinkTag {

    private Long id;
    private Long linkId;
    private Long tagId;
    private Long chatId;
}
