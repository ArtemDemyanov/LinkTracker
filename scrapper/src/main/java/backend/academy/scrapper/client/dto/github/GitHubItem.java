package backend.academy.scrapper.client.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubItem {
    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("user")
    private GitHubUser user;

    @JsonProperty("created_at")
    public Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("html_url")
    private String url;

    @JsonProperty("pull_request")
    private JsonNode pullRequest;

    public boolean isPullRequest() {
        return pullRequest != null;
    }
}
