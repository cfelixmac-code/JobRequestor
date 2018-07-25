package com.cfelixmac.req.requester

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.cfelixmac.req.requester.cache.CacheParams
import com.cfelixmac.req.requester.cache.CachePolicy
import com.cfelixmac.req.requester.cache.ICache
import io.reactivex.Observable

/**
 * @author cfelixmac at 2018.
 */
abstract class BaseJob<T : Any>
constructor(val identifier: String,
            val bus: IBus?,
            val type: Int = TYPE_PARALLEL,
            val cachePolicy: CachePolicy = CachePolicy.NO_CACHE,
            callback: RequestCallback<T>? = null,
            val group: String? = null,
            priority: Int = 1) {

    @Suppress("unused")
    @JvmOverloads
    constructor(identifier: String, bus: IBus? = null, config: Config, callback: RequestCallback<T>? = null) :
            this(identifier = identifier,
                    bus = bus,
                    callback = callback,
                    type = config.type,
                    cachePolicy = config.cachePolicy,
                    group = config.group,
                    priority = config.priority)

    private val job: InternalJob<T>

    init {
        val params = Params(priority).addTags(identifier)
        this.job = InternalJob(params, identifier, type, cachePolicy, bus, callback)
    }

    abstract fun buildRequestObservable(): Observable<T>

    open fun buildCacheParams(): CacheParams? = null

    internal fun job(): Job {
        this.job.setRequest(buildRequestObservable())
        return this.job
    }

    internal fun setCache(cache: ICache?) {
        this.job.cache = cache
        this.job.cacheParams = buildCacheParams()
    }

    class Config {
        var type: Int = TYPE_PARALLEL
            private set

        var cachePolicy: CachePolicy = CachePolicy.NO_CACHE
            private set

        var group: String? = null

        var priority: Int = 1

        fun type(type: Int) = apply { this.type = type }

        fun cachePolicy(cachePolicy: CachePolicy) = apply { this.cachePolicy = cachePolicy }

        fun group(group: String?) = apply { this.group = group }

        fun priority(priority: Int) = apply { this.priority = priority }
    }
}