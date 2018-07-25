package com.cfelixmac.req.requester.cache

/**
 * @author cfelixmac at 2018.
 */
class CacheParams private constructor(
        var expireTime: Long, var expireDuration: Long) {

    class Builder {

        var expireTime: Long = -1
        var expireDuration: Long = -1

        fun expireTime(expireTime: Long): Builder = also {
            it.expireTime = expireTime
        }

        fun expireDuration(expireDuration: Long): Builder = also {
            it.expireDuration = expireDuration
        }

        fun build(): CacheParams = CacheParams(expireTime, expireDuration)
    }
}

