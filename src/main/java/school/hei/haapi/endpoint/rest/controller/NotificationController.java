package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.NotificationMapper;
import school.hei.haapi.endpoint.rest.model.Notification;
import school.hei.haapi.endpoint.rest.model.NotificationResolutionStatus;
import school.hei.haapi.endpoint.rest.model.UpdateNotification;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.sms.NotificationService;

@RestController
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;
  private final NotificationMapper notificationMapper;

  @GetMapping("/notifications")
  public List<Notification> getNotifications(
      @AuthenticationPrincipal Principal principal,
      @RequestParam(required = false) Boolean read,
      @RequestParam(name = "resolution_status", required = false)
          NotificationResolutionStatus resolutionStatus,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return notificationService
        .getByCriteria(
            principal.getUserId(),
            read,
            notificationMapper.toDomain(resolutionStatus),
            PageRequest.of(page.getValue() - 1, pageSize.getValue()))
        .stream()
        .map(notificationMapper::toRest)
        .toList();
  }

  @GetMapping("/notifications/{id}")
  public Notification getNotificationById(@PathVariable("id") String id) {
    return notificationMapper.toRest(notificationService.getById(id));
  }

  @PutMapping("/notifications/{id}")
  public Notification updateNotification(
      @PathVariable("id") String id, @RequestBody UpdateNotification toUpdate) {
    return notificationMapper.toRest(
        notificationService.update(
            id, toUpdate.getRead(), notificationMapper.toDomain(toUpdate.getResolutionStatus())));
  }
}
