package backend.academy.scrapper.service.update;

import backend.academy.dto.request.LinkUpdateRequest;
import backend.academy.dto.response.LinkResponse;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.dto.github.GitHubItem;
import backend.academy.scrapper.notification.NotificationSender;
import backend.academy.scrapper.service.link.LinkService;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class GitHubLinkUpdateHandler implements LinkUpdateHandler {

    private final GitHubClient gitHubClient;
    private final LinkService linkService;
    private final NotificationSender notificationSender;
    private static final Pattern TRIM_SLASHES = Pattern.compile("^/+|/+$");

    public GitHubLinkUpdateHandler(
            GitHubClient gitHubClient, LinkService linkService, NotificationSender notificationSender) {
        this.gitHubClient = gitHubClient;
        this.linkService = linkService;
        this.notificationSender = notificationSender;
    }

    @Override
    public boolean supports(URI url) {
        return "github.com".equalsIgnoreCase(url.getHost());
    }

    @Override
    public void handle(LinkResponse link) {
        String[] pathParts =
                TRIM_SLASHES.matcher(link.url().getPath()).replaceAll("").split("/+", -1);
        if (pathParts.length < 2) return;

        String owner = pathParts[0];
        String repo = pathParts[1];
        Instant lastProcessed = Instant.parse(linkService.getLastUpdated(link.id()));

        gitHubClient.fetchGitHubItems("issues", owner, repo, lastProcessed).subscribe(items -> {
            if (!items.isEmpty()) {
                for (GitHubItem item : items) {
                    String author = item.user().login();
                    if (shouldIgnoreByFilter(link, author)) {
                        continue;
                    }
                    String message =
                            item.isPullRequest() ? formatPRMessage(link, item) : formatIssueMessage(link, item);

                    notificationSender.sendNotification(new LinkUpdateRequest(
                            link.id(), link.url(), message, linkService.getChatIdsByLinkId(link.id())));
                }

                Instant newest = items.stream()
                        .map(GitHubItem::createdAt)
                        .max(Comparator.naturalOrder())
                        .orElse(Instant.now());

                linkService.updateLastUpdated(link.id(), newest.toString());
            }
        });
    }

    private boolean shouldIgnoreByFilter(LinkResponse link, String author) {
        return link.filters().stream()
                .filter(f -> f.startsWith("user:"))
                .map(f -> f.substring("user:".length()))
                .anyMatch(filtered -> filtered.equalsIgnoreCase(author));
    }

    private String formatIssueMessage(LinkResponse link, GitHubItem issue) {
        return String.format(
                "\uD83D\uDCE2 Update for: %s%n\uD83D\uDD17 Link: %s%n\uD83D\uDCDD New Issue: %s%n\uD83D\uDC64 Author: %s%n⏰ Created: %s%n\uD83D\uDCC4 Description: %s",
                link.url(),
                issue.url(),
                issue.title(),
                issue.user().login(),
                issue.createdAt(),
                truncate(issue.body(), 200));
    }

    private String formatPRMessage(LinkResponse link, GitHubItem pr) {
        return String.format(
                "\uD83D\uDCE2 Update for: %s%n\uD83D\uDD17 Link: %s%n🔄 New PR: %s%n\uD83D\uDC64 Author: %s%n⏰ Created: %s%n\uD83D\uDCC4 Description: %s",
                link.url(), pr.url(), pr.title(), pr.user().login(), pr.createdAt(), truncate(pr.body(), 200));
    }

    private String truncate(String text, int limit) {
        return (text == null) ? "No description" : text.length() > limit ? text.substring(0, limit) + "..." : text;
    }
}
