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
@JvmOverloads constructor(val identifier: String,
                          val bus: IBus?,
                          val type: Int = TYPE_PARALLEL,
                          val cachePolicy: CachePolicy = CachePolicy.NO_CACHE,
                          callback: RequestCallback<T>? = null,
                          val group: String? = null,
                          priority: Int = 1) {

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
}