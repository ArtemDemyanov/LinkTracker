package backend.academy.scrapper.client.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponse {
    @JsonProperty("items")
    public List<Comment> items = Collections.emptyList();
}
