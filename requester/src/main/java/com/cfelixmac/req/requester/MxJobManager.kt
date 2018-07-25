package com.cfelixmac.req.requester

import android.content.Context
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.TagConstraint
import com.cfelixmac.req.requester.JobManagerBuilder.Companion.DEFAULT_PARALLEL_THREADS_COUNT
import com.cfelixmac.req.requester.cache.CachePolicy
import com.cfelixmac.req.requester.cache.DefaultCacheImpl
import com.cfelixmac.req.requester.cache.ICache
import com.cfelixmac.req.requester.event.CacheFailEvent
import com.cfelixmac.req.requester.event.ResultEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * RJobManager
 * entry for all jobs to manage, should be singleton.
 *
 * @author cfelixmac at 2018.
 */
const val TYPE_UNDEFINED = 0
const val TYPE_SERIAL = 1
const val TYPE_PARALLEL = 2

class MxJobManager private constructor(private val context: Context, val cache: ICache?) {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun create(context: Context, useCache: Boolean = false, cache: ICache? = null): MxJobManager {
            if (useCache && cache == null) {
                return MxJobManager(context, DefaultCacheImpl(context))
            }
            return MxJobManager(context, cache)
        }
    }

    private var parallelManager: JobManager? = null                 // manager for parallel jobs, single
    private val serialManagersMap = HashMap<String, JobManager>()   // managers for serial jobs, map key -> job group name

    private val parallelIdentifiers = HashSet<String>()                    // identifiers for parallel jobs
    private val serialIdentifiersMap = HashMap<String, HashSet<String>>()  // identifiers for serial jobs, map key -> job group name

    @JvmOverloads
    fun <T : Any> sendJob(job: BaseJob<T>, objClass: Class<T>? = null) {
        val jobManager = getJobManager(job.group, job.type, job.identifier)
        job.setCache(cache)
        when (job.cachePolicy) {
            CachePolicy.ALWAYS_CACHE -> {
                if (cache!!.has(job.identifier)) {
                    cache.read(job.identifier, objClass!!).subscribe(object : Observer<T> {
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onNext(t: T) {
                            job.bus?.post(ResultEvent(identifier = job.identifier,
                                    type = job.type, fromCache = true,
                                    isSuccess = true, result = t))
                        }

                        override fun onError(e: Throwable) {
                            job.bus?.post(CacheFailEvent(identifier = job.identifier,
                                    type = job.type, e = e))
                            jobManager?.addJobInBackground(job.job())
                        }

                        override fun onComplete() {
                        }
                    })

                } else {
                    jobManager?.addJobInBackground(job.job())
                }
            }
            CachePolicy.UPDATE_CACHE -> {
                if (cache!!.has(job.identifier)) {
                    cache.read(job.identifier, objClass!!).subscribe(object : Observer<T> {
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onNext(t: T) {
                            job.bus?.post(ResultEvent(identifier = job.identifier,
                                    type = job.type, fromCache = true,
                                    isSuccess = true, result = t))
                            jobManager?.addJobInBackground(job.job())
                        }

                        override fun onError(e: Throwable) {
                            job.bus?.post(CacheFailEvent(identifier = job.identifier,
                                    type = job.type, e = e))
                            jobManager?.addJobInBackground(job.job())
                        }

                        override fun onComplete() {
                        }
                    })
                } else {
                    jobManager?.addJobInBackground(job.job())
                }
            }
            CachePolicy.NO_CACHE -> {
                jobManager?.addJobInBackground(job.job())
            }
            else -> {
                jobManager?.addJobInBackground(job.job())
            }
        }
    }

    private fun getJobManager(group: String?, type: Int, identifier: String): JobManager? {
        when (type) {
            TYPE_PARALLEL -> {
                parallelIdentifiers.add(identifier)
                return parallelManager ?: JobManagerBuilder(context)
                        .type(TYPE_PARALLEL)
                        .maxParallelThreadsCount(DEFAULT_PARALLEL_THREADS_COUNT)
                        .build()
            }
            TYPE_SERIAL -> {
                val groupName: String = group ?: "DEFAULT_GROUP"
                if (serialManagersMap[groupName] == null) {
                    serialManagersMap[groupName] = JobManagerBuilder(context)
                            .type(TYPE_SERIAL)
                            .build()
                }
                if (serialIdentifiersMap[groupName] == null) {
                    serialIdentifiersMap[groupName] = HashSet()
                }
                serialIdentifiersMap[groupName]?.add(identifier)
                return serialManagersMap[groupName]
            }
            else -> {
                throw IllegalArgumentException("unsupported job manager type: $type")
            }
        }
    }

    fun clear() {
        parallelManager?.cancelJobsInBackground(null, TagConstraint.ALL,
                parallelIdentifiers.toArray(arrayOfNulls<String>(parallelIdentifiers.size)))
        for ((key, _) in serialIdentifiersMap) {
            val identifiers: HashSet<String>? = serialIdentifiersMap[key]
            serialManagersMap[key]?.cancelJobsInBackground(null, TagConstraint.ALL,
                    identifiers?.toArray(arrayOfNulls<String>(identifiers.size)))
        }
    }
}