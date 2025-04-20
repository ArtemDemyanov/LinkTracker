package backend.academy.scrapper.client.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Answer {
    @JsonProperty("creation_date")
    public long creationDate;

    public String body = "";

    public Owner owner = new Owner();

    @JsonProperty("answer_id")
    public long answerId;
}
