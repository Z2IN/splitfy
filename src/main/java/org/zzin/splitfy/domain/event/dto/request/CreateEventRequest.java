package org.zzin.splitfy.domain.event.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateEventRequest(
    @NotBlank(message = "이벤트 제목 입력은 필수입니다.")
    @Size(max = 100, message = "이벤트 제목은 최대 100자까지 입력이 가능합니다.")
    String title,

    @NotBlank(message = "이벤트 내용 입력은 필수입니다.")
    String description,

    @NotNull(message = "이벤트 시작 시간 입력은 필수입니다.")
    @FutureOrPresent(message = "이벤트 시작 시간은 현재 시간 이후여야 합니다.")
    LocalDateTime startAt,

    @NotNull(message = "이벤트 마감 시간 입력은 필수입니다.")
    @Future(message = "이벤트 종료 시간은 현재 시간 이후여야 합니다.")
    LocalDateTime endAt,

    @NotNull(message = "이벤트 재고 입력은 필수입니다.")
    @Positive(message = "이벤트 재고는 1 이상의 양수여야 합니다.")
    Long totalStock
) {

}
