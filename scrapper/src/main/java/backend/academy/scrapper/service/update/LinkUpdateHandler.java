package backend.academy.scrapper.service.update;

import backend.academy.dto.response.LinkResponse;
import java.net.URI;

public interface LinkUpdateHandler {
    boolean supports(URI url);

    void handle(LinkResponse link);
}
