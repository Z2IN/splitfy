package org.zzin.splitfy.domain.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.event.entity.QWaitingQueue;

@Repository
@RequiredArgsConstructor
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


}