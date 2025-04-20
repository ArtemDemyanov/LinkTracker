package backend.academy.scrapper.service.update;

import backend.academy.dto.request.LinkUpdateRequest;
import backend.academy.dto.response.LinkResponse;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.dto.stackoverflow.Answer;
import backend.academy.scrapper.client.dto.stackoverflow.Comment;
import backend.academy.scrapper.notification.NotificationSender;
import backend.academy.scrapper.service.link.LinkService;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class StackOverflowLinkUpdateHandler implements LinkUpdateHandler {

    private final StackOverflowClient stackOverflowClient;
    private final LinkService linkService;
    private final NotificationSender notificationSender;

    public StackOverflowLinkUpdateHandler(
            StackOverflowClient stackOverflowClient, LinkService linkService, NotificationSender notificationSender) {
        this.stackOverflowClient = stackOverflowClient;
        this.linkService = linkService;
        this.notificationSender = notificationSender;
    }

    @Override
    public boolean supports(URI url) {
        return "stackoverflow.com".equalsIgnoreCase(url.getHost());
    }

    @Override
    public void handle(LinkResponse link) {
        String questionId = stackOverflowClient.extractQuestionId(link.url());
        Instant lastProcessed = Instant.parse(linkService.getLastActivityDate(link.id()));

        stackOverflowClient
                .getNewAnswers(questionId, lastProcessed)
                .zipWith(stackOverflowClient.getNewComments(questionId, lastProcessed))
                .subscribe(tuple -> {
                    List<Answer> answers = tuple.getT1();
                    List<Comment> comments = tuple.getT2();

                    if (!answers.isEmpty() || !comments.isEmpty()) {
                        for (Answer answer : answers) {
                            String author = answer.owner != null ? answer.owner.displayName : "Unknown";
                            if (shouldIgnoreByFilter(link, author)) continue;
                            send(link, formatAnswer(link, answer));
                        }
                        for (Comment comment : comments) {
                            String author = comment.owner != null ? comment.owner.displayName : "Unknown";
                            if (shouldIgnoreByFilter(link, author)) continue;
                            send(link, formatComment(link, comment));
                        }

                        Instant newest = Stream.concat(
                                        answers.stream().map(a -> Instant.ofEpochSecond(a.creationDate)),
                                        comments.stream().map(c -> Instant.ofEpochSecond(c.creationDate)))
                                .max(Instant::compareTo)
                                .orElse(Instant.now());

                        linkService.updateLastActivityDate(link.id(), newest.toString());
                    }
                });
    }

    private void send(LinkResponse link, String message) {
        List<Long> chatIds = linkService.getChatIdsByLinkId(link.id());
        if (!chatIds.isEmpty()) {
            notificationSender.sendNotification(new LinkUpdateRequest(link.id(), link.url(), message, chatIds));
        }
    }

    private boolean shouldIgnoreByFilter(LinkResponse link, String author) {
        return link.filters().stream()
                .filter(f -> f.startsWith("user:"))
                .map(f -> f.substring("user:".length()))
                .anyMatch(filtered -> filtered.equalsIgnoreCase(author));
    }

    private String formatAnswer(LinkResponse link, Answer answer) {
        String author = answer.owner != null ? answer.owner.displayName : "Unknown";
        return String.format(
                "\uD83D\uDCE2 Update for: %s%n\uD83D\uDD17 Answer Link: https://stackoverflow.com/a/%d%n💡 New Answer%n\uD83D\uDC64 Author: %s%n⏰ Created: %s%n\uD83D\uDCC4 Content: %s",
                link.url(),
                answer.answerId,
                author,
                Instant.ofEpochSecond(answer.creationDate),
                truncate(answer.body, 200));
    }

    private String formatComment(LinkResponse link, Comment comment) {
        String author = comment.owner != null ? comment.owner.displayName : "Unknown";
        String baseUrl = link.url().toString().split("#", 2)[0];
        return String.format(
                "\uD83D\uDCE2 Update for: %s%n\uD83D\uDD17 Comment Link: %s#comment%d_%d%n💬 New Comment%n\uD83D\uDC64 Author: %s%n⏰ Created: %s%n\uD83D\uDCC4 Content: %s",
                link.url(),
                baseUrl,
                comment.commentId,
                comment.commentId,
                author,
                Instant.ofEpochSecond(comment.creationDate),
                truncate(comment.body, 200));
    }

    private String truncate(String text, int limit) {
        return (text == null) ? "No description" : text.length() > limit ? text.substring(0, limit) + "..." : text;
    }
}
