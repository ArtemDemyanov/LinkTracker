package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.dto.GitHubItem;
import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.service.request.LinkUpdateRequest;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UpdateCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCheckerService.class);
    private final LinkService linkService;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final WebClient webClient;
    private final int batchSize;

    public UpdateCheckerService(
            LinkService linkService,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            WebClient.Builder webClientBuilder,
            @Value("${app.update-checker.batch-size:100}") int batchSize) {
        this.linkService = linkService;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        String botBaseUrl = "http://localhost:8080";
        this.webClient = webClientBuilder.baseUrl(botBaseUrl).build();
        this.batchSize = batchSize;
    }

    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        logger.info("Checking for updates");

        int page = 0;
        Set<LinkResponse> batch;

        do {
            batch = linkService.getLinksBatch(page, batchSize);
            processBatch(batch);
            page++;
        } while (!batch.isEmpty());
    }

    private void processBatch(Set<LinkResponse> batch) {
        for (LinkResponse link : batch) {
            URI url = link.url();
            List<Long> chatIds = linkService.getChatIdsByLinkId(link.id());

            if ("github.com".equals(url.getHost())) {
                processGitHubLink(link, chatIds);
            } else if ("stackoverflow.com".equals(url.getHost())) {
                processStackOverflowLink(link, chatIds);
            }
        }
    }

    private void processGitHubLink(LinkResponse link, List<Long> chatIds) {
        String path = link.url().getPath();
        String[] pathParts = path.replaceAll("^/+|/+$", "").split("/+", -1);

        if (pathParts.length < 2) return;

        String owner = pathParts[0];
        String repo = pathParts[1];
        Instant lastProcessedTime = getLastProcessedTime(link.id(), linkService::getLastUpdated);

        gitHubClient
                .fetchGitHubItems("issues", owner, repo, lastProcessedTime)
                .subscribe(
                        items -> {
                            if (!items.isEmpty()) {
                                items.forEach(item -> {
                                    String message = item.isPullRequest()
                                            ? formatGitHubPR(link, item)
                                            : formatGitHubIssue(link, item);
                                    sendNotification(link, message, chatIds);
                                });

                                Instant newestTime = items.stream()
                                        .map(GitHubItem::createdAt)
                                        .max(Instant::compareTo)
                                        .orElse(Instant.now());
                                linkService.updateLastUpdated(link.id(), newestTime.toString());
                            }
                        },
                        error -> logger.error("Update check failed", error));
    }

    private void processStackOverflowLink(LinkResponse link, List<Long> chatIds) {
        String questionId = stackOverflowClient.extractQuestionId(link.url());
        Instant lastProcessedTime = getLastProcessedTime(link.id(), linkService::getLastActivityDate);

        stackOverflowClient
                .getNewAnswers(questionId, lastProcessedTime)
                .zipWith(stackOverflowClient.getNewComments(questionId, lastProcessedTime))
                .subscribe(
                        tuple -> {
                            List<StackOverflowClient.Answer> answers = tuple.getT1();
                            List<StackOverflowClient.Comment> comments = tuple.getT2();

                            if (!answers.isEmpty() || !comments.isEmpty()) {
                                processStackOverflowUpdates(link, answers, comments, chatIds);
                                updateLastProcessedTime(
                                        link.id(), answers, comments, linkService::updateLastActivityDate);
                            }
                        },
                        error -> logger.error("StackOverflow update check failed", error));
    }

    private Instant getLastProcessedTime(Long linkId, Function<Long, String> timeGetter) {
        String storedTime = timeGetter.apply(linkId);
        if (storedTime == null || storedTime.isEmpty()) {
            return Instant.now();
        }

        try {
            return Instant.parse(storedTime);
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse timestamp with standard format: {}", storedTime);
        }

        try {
            DateTimeFormatter withoutSeconds = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm")
                    .appendOffsetId()
                    .toFormatter();
            return withoutSeconds.parse(storedTime, Instant::from);
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse timestamp without seconds: {}", storedTime);
        }

        try {
            DateTimeFormatter flexibleFormatter =
                    DateTimeFormatter.ISO_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);
            return flexibleFormatter.parse(storedTime, Instant::from);
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse timestamp '{}' for link {}, using current time", storedTime, linkId);
            return Instant.now();
        }
    }

    private void processStackOverflowUpdates(
            LinkResponse link,
            List<StackOverflowClient.Answer> answers,
            List<StackOverflowClient.Comment> comments,
            List<Long> chatIds) {
        answers.forEach(answer -> sendNotification(link, formatStackOverflowAnswer(link, answer), chatIds));
        comments.forEach(comment -> sendNotification(link, formatStackOverflowComment(link, comment), chatIds));
    }

    private String normalizeTimestamp(Instant instant) {
        return instant.toString(); // Always stores in standard ISO-8601 format
    }

    private <T1, T2> void updateLastProcessedTime(
            Long linkId, List<T1> items1, List<T2> items2, BiConsumer<Long, String> updater) {
        Stream<Instant> times1 = items1.stream().map(this::getCreatedTime);
        Stream<Instant> times2 = items2.stream().map(this::getCreatedTime);

        Instant latestTime =
                Stream.concat(times1, times2).max(Instant::compareTo).orElse(Instant.now());

        updater.accept(linkId, normalizeTimestamp(latestTime));
    }

    private Instant getCreatedTime(Object item) {
        if (item instanceof GitHubItem) {
            return ((GitHubItem) item).createdAt();
        } else if (item instanceof StackOverflowClient.Answer) {
            return Instant.ofEpochSecond(((StackOverflowClient.Answer) item).creationDate);
        } else if (item instanceof StackOverflowClient.Comment) {
            return Instant.ofEpochSecond(((StackOverflowClient.Comment) item).creationDate);
        }
        return Instant.now();
    }

    private String formatGitHubIssue(LinkResponse link, GitHubItem issue) {
        return String.format(
                "📢 Update for: %s%n" + "🔗 Link: %s%n"
                        + "📝 New Issue: %s%n"
                        + "👤 Author: %s%n"
                        + "⏰ Created: %s%n"
                        + "📄 Description: %s",
                link.url(),
                issue.url(),
                issue.title(),
                issue.user().login(),
                issue.createdAt(),
                truncate(issue.body(), 200));
    }

    private String formatGitHubPR(LinkResponse link, GitHubItem pr) {
        return String.format(
                "📢 Update for: %s%n" + "🔗 Link: %s%n"
                        + "🔄 New PR: %s%n"
                        + "👤 Author: %s%n"
                        + "⏰ Created: %s%n"
                        + "📄 Description: %s",
                link.url(), pr.url(), pr.title(), pr.user().login(), pr.createdAt(), truncate(pr.body(), 200));
    }

    private String formatStackOverflowAnswer(LinkResponse link, StackOverflowClient.Answer answer) {
        String authorName =
                (answer.owner != null && answer.owner.displayName != null) ? answer.owner.displayName : "Unknown";
        return String.format(
                "📢 Update for: %s%n" + "🔗 Answer Link: https://stackoverflow.com/a/%d%n"
                        + "💡 New Answer%n"
                        + "👤 Author: %s%n"
                        + "⏰ Created: %s%n"
                        + "📄 Content: %s",
                link.url(),
                answer.answerId,
                authorName,
                Instant.ofEpochSecond(answer.creationDate),
                truncate(answer.body, 200));
    }

    private String formatStackOverflowComment(LinkResponse link, StackOverflowClient.Comment comment) {
        String authorName =
                (comment.owner != null && comment.owner.displayName != null) ? comment.owner.displayName : "Unknown";
        String urlStr = link.url().toString();
        String baseUrl = urlStr.split("#", 2)[0];
        return String.format(
                "📢 Update for: %s%n" + "🔗 Comment Link: %s#comment%d_%d%n"
                        + "💬 New Comment%n"
                        + "👤 Author: %s%n"
                        + "⏰ Created: %s%n"
                        + "📄 Content: %s",
                link.url(),
                baseUrl,
                comment.commentId,
                comment.commentId,
                authorName,
                Instant.ofEpochSecond(comment.creationDate),
                truncate(comment.body, 200));
    }

    private String truncate(String text, int length) {
        if (text == null) {
            return "No description";
        }
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }

    private void sendNotification(LinkResponse link, String message, List<Long> chatIds) {
        if (chatIds.isEmpty()) {
            return;
        }

        LinkUpdateRequest request = new LinkUpdateRequest(link.id(), link.url(), message, chatIds);

        webClient
                .post()
                .uri("/links")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> logger.error("Failed to send notification", e))
                .subscribe();
    }
}
