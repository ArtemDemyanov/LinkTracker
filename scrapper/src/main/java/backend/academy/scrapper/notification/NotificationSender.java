package backend.academy.scrapper.notification;

import backend.academy.dto.request.LinkUpdateRequest;

public interface NotificationSender {
    void sendNotification(LinkUpdateRequest request);
}
