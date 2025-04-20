package backend.academy.scrapper.service.processor;

import backend.academy.dto.response.LinkResponse;
import java.util.Set;

public interface LinkProcessor {
    void process(Set<LinkResponse> links);
}
