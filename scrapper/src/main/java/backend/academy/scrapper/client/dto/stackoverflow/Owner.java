package backend.academy.scrapper.client.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Owner {
    @JsonProperty("display_name")
    public String displayName;

    @JsonProperty("user_id")
    public long userId;
}
