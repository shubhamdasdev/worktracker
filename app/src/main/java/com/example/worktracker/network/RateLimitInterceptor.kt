package com.example.worktracker.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OkHttp Interceptor that handles rate limiting (HTTP 429) responses.
 * It implements exponential backoff for retries and respects Retry-After headers.
 */
class RateLimitInterceptor : Interceptor {
    private val TAG = "RateLimitInterceptor"
    private val MAX_RETRIES = 3
    private val BASE_DELAY_MS = 1000L // 1 second base delay

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var retryCount = 0

        while (!response.isSuccessful && response.code == 429 && retryCount < MAX_RETRIES) {
            retryCount++
            
            // Check if we have a Retry-After header
            val retryAfterHeader = response.header("Retry-After")
            val delaySeconds = retryAfterHeader?.toLongOrNull() 
                ?: calculateExponentialBackoff(retryCount)
            
            Log.w(TAG, "Rate limited (429). Retry $retryCount after $delaySeconds seconds")
            
            // Close the previous response to avoid resource leaks
            response.close()
            
            // Wait before retrying
            try {
                TimeUnit.SECONDS.sleep(delaySeconds)
            } catch (e: InterruptedException) {
                throw IOException("Rate limit retry interrupted", e)
            }
            
            // Retry the request
            response = chain.proceed(request)
        }
        
        return response
    }
    
    /**
     * Calculate exponential backoff delay based on retry count.
     * Formula: BASE_DELAY * 2^(retryCount-1) with some jitter
     */
    private fun calculateExponentialBackoff(retryCount: Int): Long {
        val exponentialDelay = BASE_DELAY_MS * Math.pow(2.0, (retryCount - 1).toDouble()).toLong()
        val jitter = (Math.random() * 0.3 * exponentialDelay).toLong() // Add 0-30% jitter
        return TimeUnit.MILLISECONDS.toSeconds(exponentialDelay + jitter)
    }
}
