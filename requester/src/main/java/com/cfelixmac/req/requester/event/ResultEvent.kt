package com.cfelixmac.req.requester.event

import com.cfelixmac.req.requester.TYPE_UNDEFINED

/**
 * @author cfelixmac at 2018.
 */
class ResultEvent<T : Any>(@JvmField val identifier: String? = null,
                           @JvmField val type: Int = TYPE_UNDEFINED,
                           @JvmField val fromCache: Boolean = false,
                           @JvmField val isSuccess: Boolean = true,
                           @JvmField val result: T? = null,
                           @JvmField val e: Throwable? = null)