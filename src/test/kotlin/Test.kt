import io.ktor.client.call.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import top.shoco.mocha.Mocha
import top.shoco.mocha.Query
import top.shoco.mocha.Route
import kotlin.test.Test
import kotlin.test.assertEquals

object MochaTest {
    private val mocha = Mocha(KtorClient, "http://localhost:8080")

    init {
        createTestServer()
    }

    @Test
    fun testRoute() = runBlocking {
        val body: RequestInfo = mocha.request(
            route = Route.Get("some/path")
        ).body()
        assertEquals("GET", body.method)
        assertEquals("/some/path", body.path)
    }

    @Test
    fun testQuery() = runBlocking {
        val body: RequestInfo = mocha.request(
            route = Route.Get(),
            query = Query(
                "name" to "Alice",
                "name" to "Bob",
                "age" to 20,
            ),
        ).body()
        assertEquals(
            mapOf(
                "name" to listOf("Alice", "Bob"),
                "age" to listOf("20"),
            ),
            body.query,
        )
    }
}

data class RequestInfo(
    var method: String = "",
    var path: String = "",
    var headers: Map<String, String> = emptyMap(),
    var query: Map<String, List<String>> = emptyMap(),
)

fun createTestServer() {
    embeddedServer(Netty, host = "localhost", port = 8080) {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            route("{...}") {
                handle {
                    call.respond(
                        RequestInfo(
                            call.request.httpMethod.value,
                            call.request.path(),
                            call.request.headers.toMap().mapValues { (_, v) -> v.first() },
                            call.request.queryParameters.toMap(),
                        )
                    )
                }
            }
        }
    }.start()
}
