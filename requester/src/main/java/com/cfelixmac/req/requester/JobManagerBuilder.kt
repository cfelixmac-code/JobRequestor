package com.cfelixmac.req.requester

import android.content.Context
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration

/**
 * @author cfelixmac at 2018.
 */
internal class JobManagerBuilder(private val context: Context) {

    var type: Int = TYPE_PARALLEL
    private var maxParallelThreadsCount: Int = DEFAULT_PARALLEL_THREADS_COUNT

    companion object {
        const val DEFAULT_PARALLEL_THREADS_COUNT = 5
    }

    fun type(type: Int): JobManagerBuilder = also {
        it.type = type
    }

    fun maxParallelThreadsCount(count: Int) = also {
        it.maxParallelThreadsCount = count
    }

    fun build() = JobManager(
            Configuration.Builder(context)
                    .maxConsumerCount(if (type == TYPE_SERIAL) 1 else maxParallelThreadsCount)
                    .build()
    )
}