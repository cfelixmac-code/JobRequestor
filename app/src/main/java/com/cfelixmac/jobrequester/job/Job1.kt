package com.cfelixmac.jobrequester.job

import com.cfelixmac.jobrequester.DemoApi
import com.cfelixmac.req.requester.BaseJob
import com.cfelixmac.req.requester.IBus
import com.cfelixmac.req.requester.RequestCallback
import com.cfelixmac.req.requester.TYPE_PARALLEL
import com.cfelixmac.req.requester.cache.CacheParams
import com.cfelixmac.req.requester.cache.CachePolicy
import com.google.gson.JsonElement

import io.reactivex.Observable

class Job1 @JvmOverloads constructor(private val api: DemoApi,
                                     id: String,
                                     bus: IBus,
                                     type: Int = TYPE_PARALLEL,
                                     cachePolicy: CachePolicy = CachePolicy.NO_CACHE,
                                     callback: RequestCallback<JsonElement>? = null,
                                     group: String? = null,
                                     priority: Int = 1) : BaseJob<JsonElement>(id, bus, type, cachePolicy, callback, group, priority) {

    override fun buildRequestObservable(): Observable<JsonElement> {
        return api.singlePost
    }

    override fun buildCacheParams(): CacheParams? {
        return CacheParams.Builder().expireDuration(2000).build()
    }
}
