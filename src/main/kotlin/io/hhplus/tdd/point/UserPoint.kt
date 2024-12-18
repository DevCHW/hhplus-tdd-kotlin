package io.hhplus.tdd.point

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {

    init {
        require(point >= MIN_POINT) {
            throw IllegalArgumentException("유저 포인트는 ${MIN_POINT} 미만일 수 없습니다.")
        }

        require(point <= MAX_POINT) {
            throw IllegalArgumentException("유저 포인트는 ${MAX_POINT} 포인트를 초과할 수 없습니다.")
        }
    }

    companion object {
        const val MAX_POINT = 1_000_000L
        const val MIN_POINT = 0L
    }

}
