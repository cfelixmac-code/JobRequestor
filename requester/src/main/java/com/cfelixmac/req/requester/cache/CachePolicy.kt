package com.cfelixmac.req.requester.cache

/**
 * @author cfelixmac at 2018.
 */
enum class CachePolicy {
    ALWAYS_CACHE,   // always return cache, if no cache, store web data to cache
    UPDATE_CACHE,   // always return cache first, then request from server, return new data again, update the cache
    NO_CACHE,       // always return data from server, no cache strategy
    ONLY_UPDATE     // request data from server, save data to cache, no return
}