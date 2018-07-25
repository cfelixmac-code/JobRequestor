package com.cfelixmac.req.requester

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import com.cfelixmac.req.requester.cache.CacheParams
import com.cfelixmac.req.requester.cache.CachePolicy
import com.cfelixmac.req.requester.cache.ICache
import com.cfelixmac.req.requester.event.ResultEvent
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * @author cfelixmac at 2018.
 */
internal class InternalJob<T : Any>(params: Params,
                                    private val identifier: String,
                                    private val type: Int = TYPE_UNDEFINED,
                                    private val cachePolicy: CachePolicy?,
                                    private val rxBus: IBus?,
                                    private val callback: RequestCallback<T>? = null) : Job(params) {

    private var disposable: Disposable? = null
    private var request: Observable<T>? = null

    internal var cache: ICache? = null
    internal var cacheParams: CacheParams? = null

    fun setRequest(request: Observable<T>?) {
        this.request = request
    }

    override fun onAdded() {
    }

    override fun onRun() {
        request?.subscribe(object : Observer<T> {
            override fun onNext(t: T) {
                var shouldReturn = true
                when (cachePolicy) {
                    CachePolicy.ALWAYS_CACHE, CachePolicy.UPDATE_CACHE -> cache?.save(identifier, t)

                    CachePolicy.ONLY_UPDATE -> {
                        cache?.save(identifier, t)
                        shouldReturn = false
                    }

                    else -> {
                    }
                }
                if (shouldReturn) {
                    rxBus?.post(ResultEvent(identifier = identifier, type = type,
                            fromCache = false, isSuccess = true, result = t))
                    callback?.onSuccess(t)
                }
            }

            override fun onError(e: Throwable) {
                rxBus?.post(ResultEvent<T>(identifier = identifier, type = type,
                        fromCache = false, isSuccess = false, e = e))
                callback?.onFailed(e)
            }

            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
                disposable = d
            }
        })
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        disposable?.dispose()
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint? {
        return null
    }
}