package com.cfelixmac.req.requester.event

import com.cfelixmac.req.requester.TYPE_UNDEFINED

class CacheFailEvent(@JvmField val identifier: String? = null,
                     @JvmField val type: Int = TYPE_UNDEFINED,
                     @JvmField val e: Throwable? = null)