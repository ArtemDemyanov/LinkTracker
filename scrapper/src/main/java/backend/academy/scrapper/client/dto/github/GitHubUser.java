package backend.academy.scrapper.client.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubUser {
    @JsonProperty("login")
    private String login;

    @JsonProperty("html_url")
    private String profileUrl;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
