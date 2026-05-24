package com.github.nacabaro.vbhelper.battle

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds Authorization header to API requests.
 * Skips adding header for auth endpoints.
 */
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding auth header for auth endpoints
        if (originalRequest.url.encodedPath.startsWith("/api/auth")) {
            return chain.proceed(originalRequest)
        }
        
        // Add authentication header for game endpoints
        // Use X-Session-Token header (preferred) or Authorization: Bearer
        val authenticatedRequest = originalRequest.newBuilder()
            .header("X-Session-Token", token)
            .build()
        
        // Debug: Log which header is being used (first few chars of token for security)
        val tokenPreview = if (token.length > 8) "${token.take(4)}...${token.takeLast(4)}" else "***"
        println("AuthInterceptor: Adding X-Session-Token header (token: $tokenPreview)")
        
        return chain.proceed(authenticatedRequest)
    }
}
