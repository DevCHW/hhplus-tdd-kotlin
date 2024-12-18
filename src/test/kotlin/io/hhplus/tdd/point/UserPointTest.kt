package io.hhplus.tdd.point

import io.hhplus.tdd.point.fixture.FixtureUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserPointTest {

    @Test
    fun `유저 포인트를 생성할 수 있다`() {
        // when
        val userPoint = FixtureUtil.userPoint()

        // then
        assertThat(userPoint).isNotNull
        assertThat(userPoint).isInstanceOf(UserPoint::class.java)
    }

    @Nested
    @DisplayName("유저 포인트 초기화 테스트")
    inner class Init {
        @Test
        fun `유저 포인트는 0 미만 일 수 없다`() {
            assertThatThrownBy {
                FixtureUtil.userPoint(point = -1L)
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("유저 포인트는 0 미만일 수 없습니다.")
        }

        @Test
        fun `유저 포인트는 1,000,000 이상일 수 없다`() {
            assertThatThrownBy {
                FixtureUtil.userPoint(point = 1_000_001L)
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("유저 포인트는 1000000 포인트를 초과할 수 없습니다.")
        }
    }
}