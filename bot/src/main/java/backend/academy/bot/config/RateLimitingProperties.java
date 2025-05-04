package backend.academy.bot.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingProperties {

    private final boolean enabled;
    private final int requestsPerMinute;

    @ConstructorBinding
    public RateLimitingProperties(boolean enabled, int requestsPerMinute) {
        this.enabled = enabled;
        this.requestsPerMinute = requestsPerMinute;
    }
}
