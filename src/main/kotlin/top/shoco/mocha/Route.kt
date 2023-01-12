package top.shoco.mocha

import io.ktor.http.*

class Route(
    val method: HttpMethod,
    val path: String? = null,
) {
    constructor(method: String, path: String? = null) : this(HttpMethod(method), path)

    @Suppress("FunctionName")
    companion object {
        fun Get(path: String? = null) = Route(HttpMethod.Get, path)
        fun Post(path: String? = null) = Route(HttpMethod.Post, path)
        fun Put(path: String? = null) = Route(HttpMethod.Put, path)
        fun Patch(path: String? = null) = Route(HttpMethod.Patch, path)
        fun Delete(path: String? = null) = Route(HttpMethod.Delete, path)
        fun Head(path: String? = null) = Route(HttpMethod.Head, path)
        fun Options(path: String? = null) = Route(HttpMethod.Options, path)
    }
}
