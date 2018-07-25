package com.cfelixmac.req.requester

/**
 * @author cfelixmac at 2018.
 */
interface RequestCallback<in T> {

    fun onSuccess(t: T)

    fun onFailed(e: Throwable)
}