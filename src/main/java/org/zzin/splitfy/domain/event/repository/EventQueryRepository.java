package org.zzin.splitfy.domain.event.repository;

import static org.zzin.splitfy.domain.event.entity.QEvent.event;
import static org.zzin.splitfy.domain.event.entity.QEventNumber.eventNumber;
import static org.zzin.splitfy.domain.event.entity.QWaitingQueue.waitingQueue;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import org.zzin.splitfy.domain.event.dto.EventCursor;
import org.zzin.splitfy.domain.event.dto.EventSummaryDTO;
import org.zzin.splitfy.domain.event.entity.EventNumber;
import org.zzin.splitfy.domain.event.entity.WaitingQueue;
import org.zzin.splitfy.domain.event.enums.EventStatus;

@Repository
@RequiredArgsConstructor
@NullMarked
public class EventQueryRepository {

  private final JPAQueryFactory queryFactory;

  //내 앞 대기 인원 수 계산
  public long countAhead(long eventId, LocalDateTime joinAt, long id) {
    Long count = queryFactory
        .select(waitingQueue.count())
        .from(waitingQueue)
        .where(
            waitingQueue.eventId.eq(eventId),
            waitingQueue.joinAt.lt(joinAt)
                .or(
                    waitingQueue.joinAt.eq(joinAt)
                        .and(waitingQueue.id.lt(id))
                )
        )
        .fetchOne();

    return count == null ? 0L : count;
  }

  public List<EventSummaryDTO> getEventsByCursor(@Nullable EventCursor cursor, int size) {
    var statusPriority = statusOrder();

    var query = queryFactory
        .select(Projections.constructor(EventSummaryDTO.class,
            event.id,
            event.title,
            event.startAt,
            event.endAt,
            event.totalStock,
            event.status))
        .from(event)
        .orderBy(statusPriority.asc(), event.id.desc())
        .limit(size + 1);

    // 다음 페이지 조건(정렬: priority ASC, id DESC):
    // priority > cursorPriority OR (priority == cursorPriority AND id < cursorId)
    if (cursor != null) {
      query.where(
          statusPriority.gt(cursor.statusPriority())
              .or(statusPriority.eq(cursor.statusPriority())
                  .and(event.id.lt(cursor.eventId())))
      );
    }

    return query.fetch();
  }

  private NumberExpression<Integer> statusOrder() {
    return new CaseBuilder()
        .when(event.status.in(EventStatus.SCHEDULED, EventStatus.OPENED)).then(0)
        .when(event.status.eq(EventStatus.CLOSED)).then(1)
        .otherwise(2);
  }

  //조회용 락 없는 1순위 찾기
  public @Nullable WaitingQueue findHead(long eventId) {
    return queryFactory
        .selectFrom(waitingQueue)
        .where(waitingQueue.eventId.eq(eventId))
        .orderBy(waitingQueue.joinAt.asc(), waitingQueue.id.asc())
        .fetchFirst();
  }

  //이벤트 1순위 찾기
  public @Nullable WaitingQueue findHeadForUpdate(long eventId) {
    return queryFactory
        .selectFrom(waitingQueue)
        .where(waitingQueue.eventId.eq(eventId))
        .orderBy(waitingQueue.joinAt.asc(), waitingQueue.id.asc())
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchFirst();
  }

  //이벤트 2순위 찾기
  public @Nullable WaitingQueue findNextHeadForUpdate(long eventId, LocalDateTime headJoinAt,
      long headId) {
    return queryFactory
        .selectFrom(waitingQueue)
        .where(
            waitingQueue.eventId.eq(eventId)
                .and(
                    waitingQueue.joinAt.gt(headJoinAt)
                        .or(waitingQueue.joinAt.eq(headJoinAt).and(waitingQueue.id.gt(headId)))
                )
        )
        .orderBy(waitingQueue.joinAt.asc(), waitingQueue.id.asc())
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchFirst();
  }

  //이벤트번호 - 참여할 번호 찾기
  public @Nullable EventNumber findEventNumberForUpdate(long eventId, int number) {
    return queryFactory
        .selectFrom(eventNumber)
        .where(eventNumber.eventId.eq(eventId), eventNumber.number.eq(number))
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  public boolean existsActiveTurn(long eventId, LocalDateTime now) {
    return queryFactory
        .selectOne()
        .from(waitingQueue)
        .where(
            waitingQueue.eventId.eq(eventId),
            waitingQueue.expireAt.isNotNull(),
            waitingQueue.expireAt.gt(now)
        )
        .fetchFirst() != null;
  }

  // 이벤트 진행 중인 큐 중 가장 먼저 들어온 사람 조회
  public @Nullable WaitingQueue findNextActivatableQueue(long eventId) {
    return queryFactory
        .selectFrom(waitingQueue)
        .where(
            waitingQueue.eventId.eq(eventId),
            waitingQueue.expireAt.isNull()
        )
        .orderBy(waitingQueue.joinAt.asc(), waitingQueue.id.asc())
        .fetchFirst();
  }

  //Bulk delete로 만료된 대기열 삭제
  public long deleteExpiredQueues(LocalDateTime now) {
    return queryFactory
        .delete(waitingQueue)
        .where(waitingQueue.expireAt.isNotNull(), waitingQueue.expireAt.loe(now))
        .execute();
  }

  // 진행중인 이벤트 아이디 찾기
  public @Nullable Long findOpenedEventId(LocalDateTime now) {
    return queryFactory
        .select(event.id)
        .from(event)
        .where(
            event.status.eq(EventStatus.OPENED),
            event.startAt.loe(now),
            event.endAt.goe(now)
        )
        .fetchFirst();
  }

}