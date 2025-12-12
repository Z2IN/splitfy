package org.zzin.splitfy.domain.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzin.splitfy.common.dto.CommonResponse;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.event.dto.request.CreateEventRequest;
import org.zzin.splitfy.domain.event.dto.response.CreateEventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.dto.response.JoinQueueResponse;
import org.zzin.splitfy.domain.event.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
@PreAuthorize("isAuthenticated()")
public class EventController {

  private final EventService eventService;

  @PostMapping
  public CommonResponse<CreateEventResponse> createEvent(
      @Valid @RequestBody CreateEventRequest request) {
    CreateEventResponse response = eventService.createEvent(request);
    return CommonResponse.success(response);
  }

  @GetMapping("/{eventId}")
  public CommonResponse<EventResponse> getEvent(
      @PathVariable("eventId") Long eventId) {
    EventResponse response = eventService.getEvent(eventId);
    return CommonResponse.success(response);
  }

  @PostMapping("/{eventId}/queue")
  public CommonResponse<JoinQueueResponse> joinQueue(
      @PathVariable("eventId") Long eventId, @AuthenticationPrincipal AuthUser authUser) {
    JoinQueueResponse response = eventService.joinQueue(eventId, authUser.userId());
    return CommonResponse.success(response);
  }

}
