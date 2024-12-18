package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointHistoryTable: PointHistoryTable,
    private val userPointTable: UserPointTable,
) {

    fun getUserPoint(userId: Long): UserPoint {
        return userPointTable.selectById(userId)
    }

    fun getPointHistory(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }

    fun charge(userId: Long, amount: Long): UserPoint {
        val currentUserPoint = userPointTable.selectById(userId)
        val chargedUserPoint = userPointTable.insertOrUpdate(userId, currentUserPoint.point + amount)
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis())
        return chargedUserPoint
    }

    fun use(userId: Long, amount: Long): UserPoint {
        val currentUserPoint = userPointTable.selectById(userId)
        val usedUserPoint = userPointTable.insertOrUpdate(userId, currentUserPoint.point - amount)
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis())
        return usedUserPoint
    }

}