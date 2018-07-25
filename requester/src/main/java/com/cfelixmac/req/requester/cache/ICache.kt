package com.cfelixmac.req.requester.cache

import io.reactivex.Observable

/**
 * @author cfelixmac at 2018.
 */
interface ICache {

    fun has(identifier: String): Boolean

    fun save(identifier: String, obj: Any?)

    fun <T> read(identifier: String, objClass: Class<T>): Observable<T>

    fun delete(identifier: String)
}