package io.hhplus.tdd.point.integration

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.fixture.FixtureUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.util.ReflectionTestUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootTest
class PointServiceIntegrationTest(
    @Autowired private val pointService: PointService,
    @Autowired private val userPointTable: UserPointTable,
    @Autowired private val pointHistoryTable: PointHistoryTable,
) {

    @AfterEach
    fun tearDown() {
        val userPointData = ReflectionTestUtils.getField(userPointTable, "table") as? MutableMap<*, *>
        userPointData?.clear()
        val pointHistoryData = ReflectionTestUtils.getField(pointHistoryTable, "table") as? MutableList<*>
        pointHistoryData?.clear()
    }

    @Test
    fun `유저의 포인트를 조회할 수 있다`() {
        // given
        val userPoint = FixtureUtil.userPoint(point = 300)
        userPointTable.insertOrUpdate(userPoint.id, userPoint.point)

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
        val pointHistories = listOf(pointHistory1, pointHistory2).map { pointHistory ->
                pointHistoryTable.insert(
                    id = pointHistory.userId,
                    amount = pointHistory.amount,
                    transactionType = pointHistory.type,
                    updateMillis = pointHistory.timeMillis,
                )
            }.toList()

        // when
        val result = pointService.getPointHistory(userId)

        // then
        assertThat(result).hasSize(pointHistories.size).extracting("userId", "amount", "type", "timeMillis")
            .containsExactlyInAnyOrder(
                tuple(userId, pointHistory1.amount, pointHistory1.type, pointHistory1.timeMillis),
                tuple(userId, pointHistory2.amount, pointHistory2.type, pointHistory2.timeMillis),
            )
    }

    @Nested
    @DisplayName("유저 포인트 충전 테스트")
    inner class Charge {

        @Test
        fun `유저의 포인트를 충전할 수 있다`() {
            // given
            val userId = 1L
            val amount = 1000L

            // when
            val result = pointService.charge(1L, 1000)

            // then
            assertThat(result.id).isEqualTo(userId)
            assertThat(result.point).isEqualTo(amount)
        }

        @Test
        fun `유저의 포인트 충전시 포인트 충전 내역이 저장되어야 한다`() {
            // given
            val userId = 1L
            val amount = 1000L

            // when
            pointService.charge(userId, amount)
            val result = pointHistoryTable.selectAllByUserId(userId)

            // then
            assertThat(result)
                .hasSize(1)
                .extracting("userId", "amount", "type")
                .containsExactlyInAnyOrder(
                    tuple(userId, amount, TransactionType.CHARGE),
                )
        }

        @Test
        fun `하나의 유저에게 동시에 포인트 충전 요청이 들어오더라도 오차 없이 충전되어야 한다`() {
            // given
            val userId = 1L
            val chargeAmount = 100L
            val executeCount = 100

            // when
            executeConcurrently(executeCount) {
                pointService.charge(userId, chargeAmount)
            }
            val result = userPointTable.selectById(userId)

            // then
            assertThat(result.point).isEqualTo(chargeAmount * executeCount)
        }
    }

    @Nested
    @DisplayName("유저 포인트 사용 테스트")
    inner class Use {
        @Test
        fun `유저의 포인트를 사용할 수 있다`() {
            // given
            val userPoint = FixtureUtil.userPoint(point = 3000)
            userPointTable.insertOrUpdate(userPoint.id, userPoint.point)
            val useAmount = 2000L

            // when
            val result = pointService.use(userPoint.id, useAmount)

            // then
            assertThat(result.id).isEqualTo(userPoint.id)
            assertThat(result.point).isEqualTo(userPoint.point - useAmount)
        }

        @Test
        fun `유저의 포인트 사용 시 포인트 사용 내역이 저장되어야 한다`() {
            // given
            val userId = 1L
            val amount = 100L
            userPointTable.insertOrUpdate(userId, amount)

            // when
            pointService.use(userId, amount)
            val result = pointHistoryTable.selectAllByUserId(userId)

            // then
            assertThat(result)
                .hasSize(1)
                .extracting("userId", "amount", "type")
                .containsExactlyInAnyOrder(
                    tuple(userId, amount, TransactionType.USE),
                )
        }

        @Test
        fun `하나의 유저에게 동시에 포인트 사용 요청이 들어오더라도 오차 없이 사용되어야 한다`() {
            // given
            val userId = 1L
            val useAmount = 100L
            val beforeUsePoint = 10000L
            val executeCount = 100
            userPointTable.insertOrUpdate(userId, beforeUsePoint)

            // when
            executeConcurrently(executeCount) {
                pointService.use(userId, useAmount)
            }
            val result = userPointTable.selectById(userId)

            // then
            assertThat(result.point).isEqualTo(beforeUsePoint - (useAmount * executeCount))
        }
    }

    private fun executeConcurrently(count: Int, task: Runnable) {
        val executorService: ExecutorService = Executors.newFixedThreadPool(count)
        val futures = (1..count).map {
            CompletableFuture.runAsync(task, executorService)
        }

        CompletableFuture.allOf(*futures.toTypedArray()).join()

        executorService.shutdown()
    }
}