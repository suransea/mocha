package top.shoco.mocha

import io.ktor.client.*
import io.ktor.client.content.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class Mocha(
    val client: HttpClient = HttpClient(),
    val baseUrl: Url,
    val headers: Map<String, String> = emptyMap(),
) {
    constructor(
        client: HttpClient = HttpClient(),
        baseUrl: String,
        headers: Map<String, String> = emptyMap(),
    ) : this(client, Url(baseUrl), headers)

    suspend fun <T> request(
        route: Route,
        query: Query = Query.Empty,
        fragment: String? = null,
        headers: Map<String, String> = emptyMap(),
        body: Body = Body.Empty,
        onUpload: ProgressListener? = null,
        onDownload: ProgressListener? = null,
        transform: suspend (HttpResponse) -> T,
    ): T = client.request(
        baseUrl, route, query, fragment,
        if (this.headers.isEmpty()) headers else this.headers + headers,
        body, onUpload, onDownload, transform,
    )

    suspend fun request(
        route: Route,
        query: Query = Query.Empty,
        fragment: String? = null,
        headers: Map<String, String> = emptyMap(),
        body: Body = Body.Empty,
        onUpload: ProgressListener? = null,
        onDownload: ProgressListener? = null,
    ): HttpResponse = request(route, query, fragment, headers, body, onUpload, onDownload) { it }
}

suspend fun <T> HttpClient.request(
    url: Url,
    route: Route,
    query: Query = Query.Empty,
    fragment: String? = null,
    headers: Map<String, String> = emptyMap(),
    body: Body = Body.Empty,
    onUpload: ProgressListener? = null,
    onDownload: ProgressListener? = null,
    transform: suspend (HttpResponse) -> T,
): T = prepareRequest {
    method = route.method
    url {
        takeFrom(url)
        if (route.path != null) appendPathSegments(route.path)
        query.params.forEach { (k, v) -> parameters.append(k, v.toString()) }
        if (fragment != null) it.fragment = fragment
    }
    headers {
        headers.forEach { (k, v) -> append(k, v) }
    }
    buildBody(body)
    onUpload(onUpload)
    onDownload(onDownload)
}.execute(transform)

suspend fun HttpClient.request(
    url: Url,
    route: Route,
    query: Query = Query.Empty,
    fragment: String? = null,
    headers: Map<String, String> = emptyMap(),
    body: Body = Body.Empty,
    onUpload: ProgressListener? = null,
    onDownload: ProgressListener? = null,
): HttpResponse = request(url, route, query, fragment, headers, body, onUpload, onDownload) { it }

private fun HttpRequestBuilder.buildBody(body: Body) = when (body) {
    is Body.Empty -> {}

    is Body.Text -> {
        contentType(body.contentType)
        setBody(body.text)
    }

    is Body.Encode -> {
        contentType(body.contentType)
        setBody(body.body, body.typeInfo)
    }

    is Body.Binary -> {
        contentType(body.contentType)
        setBody(body.bytes)
    }

    is Body.Form -> {
        setBody(FormDataContent(Parameters.build {
            body.params.forEach { (k, v) -> append(k, v.toString()) }
        }))
    }

    is Body.MultiPartForm -> {
        setBody(MultiPartFormDataContent(formData {
            body.parts.forEach(::addPart)
        }))
    }
}

private fun FormBuilder.addPart(part: Body.MultiPartForm.Part) = when (part) {
    is Body.MultiPartForm.Part.Text -> {
        append(part.name, part.value)
    }

    is Body.MultiPartForm.Part.Binary -> {
        append(part.name, ChannelProvider(part.size) { part.bytes }, Headers.build {
            if (!part.filename.isNullOrEmpty()) {
                append(HttpHeaders.ContentDisposition, "filename=\"${part.filename.escapeIfNeeded()}\"")
            }
            if (part.contentType != null) {
                append(HttpHeaders.ContentType, part.contentType)
            }
        })
    }
}
