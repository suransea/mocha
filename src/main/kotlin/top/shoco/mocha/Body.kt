@file:Suppress("FunctionName")

package top.shoco.mocha

import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import java.io.File

sealed interface Body {
    object Empty : Body

    class Text(
        val text: String,
        val contentType: ContentType = ContentType.Text.Plain,
    ) : Body

    class Binary(
        val bytes: ByteReadChannel,
        val contentType: ContentType = ContentType.Application.OctetStream,
    ) : Body

    class Encode(
        val body: Any,
        val typeInfo: TypeInfo,
        val contentType: ContentType,
    ) : Body

    @JvmInline
    value class Form(val params: List<Pair<String, Any>>) : Body {
        constructor(vararg params: Pair<String, Any>) : this(params.asList())
    }

    @JvmInline
    value class MultiPartForm(val parts: List<Part>) : Body {
        constructor(vararg parts: Part) : this(parts.asList())

        sealed interface Part {
            class Text(val name: String, val value: String) : Part
            class Binary(
                val name: String,
                val bytes: ByteReadChannel,
                val contentType: ContentType? = null,
                val size: Long? = null,
                val filename: String? = null,
            ) : Part
        }
    }

    companion object {

        inline fun <reified T : Any> Encode(
            body: T,
            contentType: ContentType,
        ) = Encode(body, typeInfo<T>(), contentType)

        inline fun <reified T : Any> EncodeJson(body: T) = Encode(body, ContentType.Application.Json)

        fun Json(json: String) = Text(json, ContentType.Application.Json)

        fun File(
            file: File,
            contentType: ContentType = ContentType.Application.OctetStream,
        ) = Binary(file.readChannel(), contentType)

        fun File(
            path: String,
            contentType: ContentType = ContentType.Application.OctetStream,
        ) = File(java.io.File(path), contentType)

        fun Bytes(
            bytes: ByteArray,
            contentType: ContentType = ContentType.Application.OctetStream,
        ) = Binary(ByteReadChannel(bytes), contentType)
    }
}

fun Part(name: String, value: String): Body.MultiPartForm.Part = Body.MultiPartForm.Part.Text(name, value)

fun Part(
    name: String,
    bytes: ByteReadChannel,
    contentType: ContentType? = null,
    size: Long? = null,
    filename: String? = null,
) = Body.MultiPartForm.Part.Binary(name, bytes, contentType, size, filename)

fun Part(
    name: String,
    file: File,
    contentType: ContentType? = null,
    filename: String = file.name,
) = Part(name, file.readChannel(), contentType, file.length(), filename)

fun Part(
    name: String,
    bytes: ByteArray,
    contentType: ContentType? = null,
    filename: String? = null,
) = Part(name, ByteReadChannel(bytes), contentType, bytes.size.toLong(), filename)
