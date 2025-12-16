package org.zzin.splitfy.domain.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.zzin.splitfy.common.config.JPAuditingConfig;
import org.zzin.splitfy.common.config.QueryDSLConfig;
import org.zzin.splitfy.domain.event.dto.EventCursor;
import org.zzin.splitfy.domain.event.dto.EventSummaryDTO;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.enums.EventStatus;
import org.zzin.splitfy.domain.event.repository.EventQueryRepository;
import org.zzin.splitfy.domain.event.repository.EventRepository;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
    QueryDSLConfig.class,
    JPAuditingConfig.class,
    EventQueryRepository.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class EventQueryRepositoryTest {

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private EventQueryRepository eventQueryRepository;

  @Test
  void getEventsByCursor_첫_페이지_조회_성공() {
    // given
    // 다양한 상태의 이벤트 생성 (정렬 순서: priority ASC, id DESC)
    Event scheduled1 = createEventWithStatus("예정된 이벤트 1", EventStatus.SCHEDULED);
    Event opened1 = createEventWithStatus("진행중 이벤트 1", EventStatus.OPENED);
    Event closed1 = createEventWithStatus("종료된 이벤트 1", EventStatus.CLOSED);
    Event scheduled2 = createEventWithStatus("예정된 이벤트 2", EventStatus.SCHEDULED);

    eventRepository.save(scheduled1);
    eventRepository.save(opened1);
    eventRepository.save(closed1);
    eventRepository.save(scheduled2);
    eventRepository.flush();

    // when - cursor null로 첫 페이지 조회 (size + 1 = 3개 조회)
    List<EventSummaryDTO> result = eventQueryRepository.getEventsByCursor(null, 2);

    // then
    assertThat(result).hasSize(3); // size + 1 = 3개 조회됨 (hasNext 판단용)

    // 정렬 순서 검증: priority ASC, id DESC
    // priority 0 (SCHEDULED, OPENED): id DESC 순서로 scheduled2, opened1, scheduled1
    // priority 1 (CLOSED): closed1
    assertThat(result.get(0).getStatus()).isIn(EventStatus.SCHEDULED, EventStatus.OPENED);
    assertThat(result.get(1).getStatus()).isIn(EventStatus.SCHEDULED, EventStatus.OPENED);
    assertThat(result.get(2).getStatus()).isIn(EventStatus.SCHEDULED, EventStatus.OPENED,
        EventStatus.CLOSED);
  }

  @Test
  void getEventsByCursor_정렬_순서_확인_priority_ASC_id_DESC() {
    // given
    Event scheduled1 = createEventWithStatus("예정1", EventStatus.SCHEDULED);
    Event scheduled2 = createEventWithStatus("예정2", EventStatus.SCHEDULED);
    Event opened1 = createEventWithStatus("진행1", EventStatus.OPENED);
    Event closed1 = createEventWithStatus("종료1", EventStatus.CLOSED);
    Event closed2 = createEventWithStatus("종료2", EventStatus.CLOSED);

    eventRepository.save(scheduled1);
    eventRepository.save(scheduled2);
    eventRepository.save(opened1);
    eventRepository.save(closed1);
    eventRepository.save(closed2);
    eventRepository.flush();

    // when
    List<EventSummaryDTO> result = eventQueryRepository.getEventsByCursor(null, 10);

    // then
    // priority 0 그룹 (SCHEDULED, OPENED): id 큰 순서
    assertThat(result.get(0).getStatus()).isIn(EventStatus.SCHEDULED, EventStatus.OPENED);
    assertThat(result.get(1).getStatus()).isIn(EventStatus.SCHEDULED, EventStatus.OPENED);
    assertThat(result.get(2).getStatus()).isIn(EventStatus.SCHEDULED, EventStatus.OPENED);

    // priority 0 그룹 내에서 id DESC 확인
    assertThat(result.get(0).getEventId()).isGreaterThan(result.get(1).getEventId());
    assertThat(result.get(1).getEventId()).isGreaterThan(result.get(2).getEventId());

    // priority 1 그룹 (CLOSED): id 큰 순서
    assertThat(result.get(3).getStatus()).isEqualTo(EventStatus.CLOSED);
    assertThat(result.get(4).getStatus()).isEqualTo(EventStatus.CLOSED);

    // priority 1 그룹 내에서 id DESC 확인
    assertThat(result.get(3).getEventId()).isGreaterThan(result.get(4).getEventId());
  }

  @Test
  void getEventsByCursor_커서_기반_다음_페이지_조회() {
    // given
    Event event1 = createEventWithStatus("이벤트1", EventStatus.OPENED);
    Event event2 = createEventWithStatus("이벤트2", EventStatus.OPENED);
    Event event3 = createEventWithStatus("이벤트3", EventStatus.OPENED);
    Event event4 = createEventWithStatus("이벤트4", EventStatus.OPENED);

    eventRepository.save(event1);
    eventRepository.save(event2);
    eventRepository.save(event3);
    eventRepository.save(event4);
    eventRepository.flush();

    // when - 첫 페이지 조회
    int size = 2;
    List<EventSummaryDTO> firstPage = getPage(null, size);
    EventCursor cursor = createCursor(firstPage.get(firstPage.size() - 1));

    // 다음 페이지 조회
    List<EventSummaryDTO> secondPage = getPage(cursor, size);

    // then
    assertThat(firstPage).hasSize(2); // 실제 반환 데이터
    assertThat(secondPage).hasSize(2); // 남은 2개 조회 (4개 - 2개 = 2개)

    // 첫 페이지와 두 번째 페이지의 이벤트 ID가 겹치지 않는지 확인
    List<Long> firstPageIds = firstPage.stream()
        .map(EventSummaryDTO::getEventId)
        .toList();

    List<Long> secondPageIds = secondPage.stream()
        .map(EventSummaryDTO::getEventId)
        .toList();

    assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
  }

  @Test
  void getEventsByCursor_커서_기반_다른_priority_그룹_조회() {
    // given
    Event opened1 = createEventWithStatus("진행1", EventStatus.OPENED);
    Event opened2 = createEventWithStatus("진행2", EventStatus.OPENED);
    Event closed1 = createEventWithStatus("종료1", EventStatus.CLOSED);
    Event closed2 = createEventWithStatus("종료2", EventStatus.CLOSED);

    eventRepository.save(opened1);
    eventRepository.save(opened2);
    eventRepository.save(closed1);
    eventRepository.save(closed2);
    eventRepository.flush();

    // when - priority 0 그룹의 마지막 아이템으로 커서 생성
    List<EventSummaryDTO> firstPage = getPage(null, 2);
    EventCursor cursor = createCursor(firstPage.get(firstPage.size() - 1));

    // 다음 페이지 조회 (priority 1 그룹으로 넘어가야 함)
    List<EventSummaryDTO> secondPage = getPage(cursor, 3);

    // then
    assertThat(secondPage).isNotEmpty();

    // 두 번째 페이지는 priority 1(CLOSED) 이벤트를 포함해야 함
    boolean hasClosed = secondPage.stream()
        .anyMatch(e -> e.getStatus() == EventStatus.CLOSED);
    assertThat(hasClosed).isTrue();
  }

  //====================== 편의 메서드 ============================

  private Event createEventWithStatus(String title, EventStatus status) {
    // Validation을 피하기 위해 일단 미래 시간으로 생성
    LocalDateTime tempTime = LocalDateTime.now().plusDays(10);
    Event event = Event.builder()
        .title(title)
        .description("설명: " + title)
        .startAt(tempTime)
        .endAt(tempTime.plusDays(1))
        .totalStock(100L)
        .status(status)
        .build();

    return event;
  }

  /**
   * Repository 조회 결과에서 실제 페이지 데이터만 추출 (Service 로직과 동일)
   */
  private List<EventSummaryDTO> getPage(EventCursor cursor, int size) {
    List<EventSummaryDTO> raw = eventQueryRepository.getEventsByCursor(cursor, size);
    return raw.size() > size ? raw.subList(0, size) : raw;
  }

  /**
   * EventSummaryDTO로부터 커서 생성
   */
  private EventCursor createCursor(EventSummaryDTO event) {
    return EventCursor.of(event.getStatus().priority(), event.getEventId());
  }
}
