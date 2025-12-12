package org.zzin.splitfy.domain.event.dto.response;

import java.time.LocalDateTime;

public record JoinQueueResponse(
    long eventId,
    long position,
    LocalDateTime joinAt
) {

}
