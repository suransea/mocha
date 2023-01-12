# Mocha

A ktor client wrapper for easier usage.

## Example

```kotlin
val KtorClient = HttpClient {
    // configure your ktor client
}

data class User(
    var name: String = "",
    var age: Int = 0,
)

// define service
object ExampleService {
    private val mocha = Mocha(
        client = KtorClient,
        baseUrl = "https://example.com/api",
    )

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
}

// usage
val user = ExampleService.user(name = "Alice")
```
