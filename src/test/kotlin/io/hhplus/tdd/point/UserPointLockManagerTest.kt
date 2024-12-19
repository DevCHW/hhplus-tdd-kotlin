package io.hhplus.tdd.point

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserPointLockManagerTest {

    /**
     * 같은 User ID 동시성 제어 테스트
     */
    @Test
    fun `같은 유저의 경우 잠금 매니저를 통해 동시 실행을 제어할 수 있다`() {
        // given
        val userId = 1L
        val task = {
            UserPointLockManager.withLock(userId) {
                Thread.sleep(1000)
            }
        }

        // when
        val startTime = System.currentTimeMillis()
        executeConcurrently(2, task) // 동시 2번 실행
        val endTime = System.currentTimeMillis()

        // then
        val resultTime = endTime - startTime
        assertThat(resultTime).isGreaterThanOrEqualTo(2000)
    }

    /**
     * 다른 User ID 병렬 실행 테스트
     */
    @Test
    fun `다른 유저의 경우 잠금 매니저에 관계없이 동시 실행이 가능해야 한다`() {
        // given
        val task = { Thread.sleep(1000) }

        // when
        val startTime = System.currentTimeMillis()
        val threads = (0 until 2).map { idx ->
            Thread {
                val userId = if (idx % 2 == 0) 1L else 2L // 다른 유저 ID
                UserPointLockManager.withLock(userId, task)
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val endTime = System.currentTimeMillis()

        // then
        val resultTime = endTime - startTime
        assertThat(resultTime).isLessThan(2000)
    }

    /**
     * 동시성 테스트 유틸 메소드
     */
    private fun executeConcurrently(count: Int, task: Runnable) {
        val executorService: ExecutorService = Executors.newFixedThreadPool(count)
        val futures = (1..count).map {
            CompletableFuture.runAsync(task, executorService)
        }

        CompletableFuture.allOf(*futures.toTypedArray()).join()

        executorService.shutdown()
    }

}