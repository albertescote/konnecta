package com.konnecta.app.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Retries [block] up to [times] attempts with exponential backoff.
 * CancellationException is always rethrown immediately.
 * The final attempt lets any exception propagate to the caller.
 */
suspend fun <T> withRetry(
    times: Int = 3,
    initialDelayMs: Long = 500L,
    maxDelayMs: Long = 4_000L,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMs
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w("Request failed (attempt ${attempt + 1}/$times), retrying in ${currentDelay}ms: ${e.message}")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
    }
    return block()
}
