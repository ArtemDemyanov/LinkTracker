package backend.academy.scrapper.client.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerResponse {
    @JsonProperty("items")
    public List<Answer> items = Collections.emptyList();
}
