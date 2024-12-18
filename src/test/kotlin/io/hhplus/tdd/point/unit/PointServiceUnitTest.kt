package io.hhplus.tdd.point.unit

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.fixture.FixtureUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock

/**
 * PointService Unit Test Class
 */
class PointServiceUnitTest {

    private lateinit var pointService: PointService
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var userPointTable: UserPointTable

    @BeforeEach
    fun setUp() {
        pointHistoryTable = mock(PointHistoryTable::class.java)
        userPointTable = mock(UserPointTable::class.java)
        pointService = PointService(pointHistoryTable, userPointTable)
    }

    @Test
    fun `유저의 포인트를 조회할 수 있다`() {
        // given
        val userPoint = FixtureUtil.userPoint()
        given(userPointTable.selectById(anyLong()))
            .willReturn(userPoint)

        // when
        val result = pointService.getUserPoint(userPoint.id)

        // then
        assertThat(result.id).isEqualTo(userPoint.id)
        assertThat(result.point).isEqualTo(userPoint.point)
    }

    @Test
    fun `유저의 포인트 변경 이력을 조회할 수 있다`() {
        // given
        val userId = 1L
        val pointHistory1 = FixtureUtil.pointHistory(
            userId = userId,
            amount = 100L,
        )

        val pointHistory2 = FixtureUtil.pointHistory(
            userId = userId,
            amount = 200L,
        )

        val pointHistories = listOf(pointHistory1, pointHistory2)

        given(pointHistoryTable.selectAllByUserId(anyLong()))
            .willReturn(pointHistories)

        // when
        val result = pointService.getPointHistory(userId)

        // then
        assertThat(result).hasSize(pointHistories.size).extracting("userId", "amount", "type", "timeMillis")
            .containsExactlyInAnyOrder(
                tuple(userId, pointHistory1.amount, pointHistory1.type, pointHistory1.timeMillis),
                tuple(userId, pointHistory2.amount, pointHistory2.type, pointHistory2.timeMillis),
            )
    }

    @Test
    fun `유저의 포인트를 충전할 수 있다`() {
        // given
        val userId = 1L
        val amount = 100L
        val beforeUserPoint = FixtureUtil.userPoint(id = userId, point = 100L)
        val chargedUserPoint = FixtureUtil.userPoint(id = userId, point = beforeUserPoint.point + amount)

        given(userPointTable.selectById(anyLong()))
            .willReturn(beforeUserPoint)

        given(userPointTable.insertOrUpdate(anyLong(), anyLong()))
            .willReturn(chargedUserPoint)

        // when
        val result = pointService.charge(beforeUserPoint.id, amount)

        // then
        assertThat(result.id).isEqualTo(userId)
        assertThat(result.point).isEqualTo(beforeUserPoint.point + amount)
    }

    @Test
    fun `유저의 포인트를 사용할 수 있다`() {
        // given
        val userId = 1L
        val amount = 100L
        val beforeUserPoint = FixtureUtil.userPoint(id = userId, point = 100)
        val afterUserPoint = FixtureUtil.userPoint(id = userId, point = 100 - amount)

        given(userPointTable.selectById(anyLong()))
            .willReturn(beforeUserPoint)

        given(userPointTable.insertOrUpdate(anyLong(), anyLong()))
            .willReturn(afterUserPoint)

        // when
        val result = pointService.use(beforeUserPoint.id, amount)

        // then
        assertThat(result.id).isEqualTo(userId)
        assertThat(result.point).isEqualTo(beforeUserPoint.point - amount)
    }

}