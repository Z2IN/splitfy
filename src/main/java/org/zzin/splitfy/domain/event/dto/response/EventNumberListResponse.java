package org.zzin.splitfy.domain.event.dto.response;

import java.util.List;

public record EventNumberListResponse(
    Long eventId,
    List<EventNumberResponse> numbers
) {

}
