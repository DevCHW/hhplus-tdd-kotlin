package io.hhplus.tdd.point.fixture

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint

class FixtureUtil {

    companion object {

        /**
         * UserPoint Fixture 생성
         */
        fun userPoint(
            id: Long = 1L,
            point: Long = 100L,
            updateMillis: Long = System.currentTimeMillis(),
        ): UserPoint {
            return UserPoint(
                id = id,
                point = point,
                updateMillis = updateMillis,
            )
        }

        /**
         * PointHistory Fixture 생성
         */
        fun pointHistory(
            id: Long = 1L,
            userId: Long = 1L,
            type: TransactionType = TransactionType.CHARGE,
            amount: Long = 100L,
            timeMillis: Long = System.currentTimeMillis(),
        ): PointHistory {
            return PointHistory(
                id = id, userId = userId, type = type, amount = amount, timeMillis = timeMillis
            )
        }
    }
}