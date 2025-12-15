package org.zzin.splitfy.domain.event.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.event.dto.EventSummaryDTO;
import org.zzin.splitfy.domain.event.entity.QEvent;
import org.zzin.splitfy.domain.event.entity.QWaitingQueue;

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


  public Page<EventSummaryDTO> getEvents(int page, int size) {
    QEvent event = QEvent.event;

    var content = queryFactory
        .select(Projections.constructor(EventSummaryDTO.class,
            event.id,
            event.title,
            event.startAt,
            event.endAt,
            event.totalStock,
            event.status))
        .from(event)
        .orderBy(event.id.desc())
        .offset((long) page * size)
        .limit(size)
        .fetch();

    var total = queryFactory
        .select(event.count())
        .from(event);

    return PageableExecutionUtils.getPage(content, PageRequest.of(page, size), total::fetchOne);
  }
}