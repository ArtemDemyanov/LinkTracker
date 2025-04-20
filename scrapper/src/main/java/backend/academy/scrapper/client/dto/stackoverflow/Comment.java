package backend.academy.scrapper.client.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment {
    @JsonProperty("creation_date")
    public long creationDate;

    public String body = "";

    public Owner owner = new Owner();

    @JsonProperty("comment_id")
    public long commentId;
}
