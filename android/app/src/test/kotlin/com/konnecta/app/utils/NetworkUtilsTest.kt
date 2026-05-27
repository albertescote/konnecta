package com.konnecta.app.utils

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class NetworkUtilsTest {

    @Test
    fun `withRetry returns value on first success`() = runTest {
        var callCount = 0
        val result = withRetry { callCount++; "ok" }
        assertEquals("ok", result)
        assertEquals(1, callCount)
    }

    @Test
    fun `withRetry retries on transient failure and returns value`() = runTest {
        var callCount = 0
        val result = withRetry(times = 3, initialDelayMs = 1L) {
            callCount++
            if (callCount < 2) throw IOException("transient")
            "recovered"
        }
        assertEquals("recovered", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `withRetry exhausts all attempts and rethrows final exception`() = runTest {
        var callCount = 0
        try {
            withRetry(times = 3, initialDelayMs = 1L) {
                callCount++
                throw IOException("persistent: attempt $callCount")
            }
            fail("Expected IOException to be thrown")
        } catch (e: IOException) {
            assertEquals("persistent: attempt 3", e.message)
        }
        assertEquals(3, callCount)
    }

    @Test
    fun `withRetry calls block exactly times times before giving up`() = runTest {
        var callCount = 0
        try {
            withRetry(times = 4, initialDelayMs = 1L) {
                callCount++
                throw IOException("fail")
            }
        } catch (_: IOException) {
        }
        assertEquals(4, callCount)
    }

    @Test
    fun `withRetry with times=1 makes a single attempt with no retries`() = runTest {
        var callCount = 0
        try {
            withRetry(times = 1) {
                callCount++
                throw IOException("immediate fail")
            }
        } catch (_: IOException) {
        }
        assertEquals(1, callCount)
    }
}
