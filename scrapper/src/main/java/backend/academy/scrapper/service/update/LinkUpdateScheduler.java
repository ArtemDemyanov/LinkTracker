package backend.academy.scrapper.service.update;

import backend.academy.dto.response.LinkResponse;
import backend.academy.scrapper.service.link.LinkService;
import backend.academy.scrapper.service.processor.LinkProcessor;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LinkUpdateScheduler {

    private final LinkProcessor linkProcessor;
    private final LinkService linkService;
    private final int batchSize;

    public LinkUpdateScheduler(
            LinkProcessor linkProcessor,
            LinkService linkService,
            @Value("${app.update-checker.batch-size:100}") int batchSize) {
        this.linkProcessor = linkProcessor;
        this.linkService = linkService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        int page = 0;
        Set<LinkResponse> batch;

        do {
            batch = linkService.getLinksBatch(page, batchSize);
            linkProcessor.process(batch);
            page++;
        } while (!batch.isEmpty());
    }
}
