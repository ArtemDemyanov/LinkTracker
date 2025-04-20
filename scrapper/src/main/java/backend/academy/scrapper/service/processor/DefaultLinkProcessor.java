package backend.academy.scrapper.service.processor;

import backend.academy.dto.response.LinkResponse;
import backend.academy.scrapper.service.update.LinkUpdateHandler;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DefaultLinkProcessor implements LinkProcessor {

    private final List<LinkUpdateHandler> handlers;

    public DefaultLinkProcessor(List<LinkUpdateHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void process(Set<LinkResponse> links) {
        for (LinkResponse link : links) {
            URI url = link.url();
            handlers.stream()
                    .filter(handler -> handler.supports(url))
                    .findFirst()
                    .ifPresent(handler -> handler.handle(link));
        }
    }
}
