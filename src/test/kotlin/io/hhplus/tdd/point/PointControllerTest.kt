package io.hhplus.tdd.point

import io.hhplus.tdd.point.fixture.FixtureUtil
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PointController::class)
class PointControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var pointService: PointService

    @Test
    fun `포인트 조회 API`() {
        // given
        val userPoint = FixtureUtil.userPoint()
        given(pointService.getUserPoint(1L))
            .willReturn(userPoint)

        // when & then
        mockMvc
            .perform(
                get("/point/{id}", 1L)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(userPoint.id))
            .andExpect(jsonPath("point").value(userPoint.point))
            .andExpect(jsonPath("updateMillis").value(userPoint.updateMillis))
    }

    @Test
    fun `포인트 내역 조회 API`() {
        // given
        val pointHistory1 = FixtureUtil.pointHistory(id = 1L)
        val pointHistory2 = FixtureUtil.pointHistory(id = 1L)
        val pointHistories = listOf(pointHistory1, pointHistory1)
        given(pointService.getPointHistory(1L))
            .willReturn(pointHistories)

        // when & then
        mockMvc
            .perform(
                get("/point/{id}/histories", 1L)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("[0].id").value(pointHistory1.id))
            .andExpect(jsonPath("[0].userId").value(pointHistory1.userId))
            .andExpect(jsonPath("[0].type").value(pointHistory1.type.name))
            .andExpect(jsonPath("[0].amount").value(pointHistory1.amount))
            .andExpect(jsonPath("[0].timeMillis").value(pointHistory1.timeMillis))
            .andExpect(jsonPath("[1].id").value(pointHistory2.id))
            .andExpect(jsonPath("[1].userId").value(pointHistory2.userId))
            .andExpect(jsonPath("[1].type").value(pointHistory2.type.name))
            .andExpect(jsonPath("[1].amount").value(pointHistory2.amount))
            .andExpect(jsonPath("[1].timeMillis").value(pointHistory2.timeMillis))
    }

    @Test
    fun `포인트 충전 API`() {
        // given
        val userPoint = FixtureUtil.userPoint()
        given(pointService.charge(1L, 1000L))
            .willReturn(userPoint)

        // when & then
        mockMvc.perform(
            patch("/point/{id}/charge", 1L)
                .content("1000")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(userPoint.id))
            .andExpect(jsonPath("point").value(userPoint.point))
            .andExpect(jsonPath("updateMillis").value(userPoint.updateMillis))
    }

    @Test
    fun `포인트 사용 API`() {
        // given
        val userPoint = FixtureUtil.userPoint()
        given(pointService.use(1L, 1000L))
            .willReturn(userPoint)

        // when & then
        mockMvc.perform(
            patch("/point/{id}/use", 1L)
                .content("1000")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id").value(userPoint.id))
            .andExpect(jsonPath("point").value(userPoint.point))
            .andExpect(jsonPath("updateMillis").value(userPoint.updateMillis))
    }

}