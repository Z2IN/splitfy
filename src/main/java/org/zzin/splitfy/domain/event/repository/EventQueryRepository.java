package org.zzin.splitfy.domain.event.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.event.dto.EventCursor;
import org.zzin.splitfy.domain.event.dto.EventSummaryDTO;
import org.zzin.splitfy.domain.event.entity.QEvent;
import org.zzin.splitfy.domain.event.entity.QWaitingQueue;
import org.zzin.splitfy.domain.event.enums.EventStatus;

@Repository
@RequiredArgsConstructor
@NullMarked
public class EventQueryRepository {

  private final JPAQueryFactory queryFactory;

  public long countAhead(long eventId, LocalDateTime joinAt, long id) {
    QWaitingQueue q = QWaitingQueue.waitingQueue;

    Long count = queryFactory
        .select(q.count())
        .from(q)
        .where(
            q.eventId.eq(eventId),
            q.joinAt.lt(joinAt)
                .or(
                    q.joinAt.eq(joinAt)
                        .and(q.id.lt(id))
                )
        )
        .fetchOne();

    return count == null ? 0L : count;
  }

  public List<EventSummaryDTO> getEventsByCursor(@Nullable EventCursor cursor, int size) {
    QEvent event = QEvent.event;

    var p = statusOrder(event);

    var query = queryFactory
        .select(Projections.constructor(EventSummaryDTO.class,
            event.id,
            event.title,
            event.startAt,
            event.endAt,
            event.totalStock,
            event.status))
        .from(event)
        .orderBy(p.asc(), event.id.desc())
        .limit(size + 1);

    // 다음 페이지 조건(정렬: priority ASC, id DESC):
    // priority > cursorPriority OR (priority == cursorPriority AND id < cursorId)
    if (cursor != null) {
      query.where(
          p.gt(cursor.statusPriority())
              .or(p.eq(cursor.statusPriority())
                  .and(event.id.lt(cursor.eventId())))
      );
    }

    return query.fetch();
  }

  private NumberExpression<Integer> statusOrder(QEvent event) {
    return new CaseBuilder()
        .when(event.status.in(EventStatus.SCHEDULED, EventStatus.OPENED)).then(0)
        .when(event.status.eq(EventStatus.CLOSED)).then(1)
        .otherwise(2);
  }

}