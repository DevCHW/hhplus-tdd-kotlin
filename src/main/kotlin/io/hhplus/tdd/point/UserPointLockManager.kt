package io.hhplus.tdd.point

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object UserPointLockManager {

    private val userPointLockMap = ConcurrentHashMap<Long, ReentrantLock>()

    fun <T> withLock(userId: Long, block: () -> T): T {
        val lock = userPointLockMap.computeIfAbsent(userId) { ReentrantLock(true) }
        lock.withLock {
            return block()
        }
    }
}