import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import top.shoco.mocha.*
import java.io.File

data class User(
    var name: String = "",
    var age: Int = 0,
)

val KtorClient = HttpClient {
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println(message)
            }
        }
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        gson()
    }
}

object ExampleService {
    private val mocha = Mocha(KtorClient, baseUrl = "https://echo.hoppscotch.io/api")

    suspend fun user(name: String): User = mocha.request(
        route = Route.Get("user"),
        query = Query("name" to name),
    ).body()

    suspend fun addUser(user: User) = mocha.request(
        route = Route.Post("user"),
        body = Body.EncodeJson(user),
    )

    suspend fun submitUser(name: String, age: Int) = mocha.request(
        route = Route.Post("user"),
        body = Body.Form(
            "name" to name,
            "age" to age,
        ),
    )

    suspend fun uploadFile() = mocha.request(
        route = Route.Post("file"),
        body = Body.MultiPartForm(
            Part("name", "value"),
            Part("file", File("settings.gradle"), ContentType.Text.Plain),
        ),
    )

    suspend fun downloadFile(target: File) = mocha.request(
        route = Route.Post(),
        transform = {
            it.bodyAsChannel().copyAndClose(target.writeChannel())
            target
        },
    )
}
