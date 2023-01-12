package top.shoco.mocha

@JvmInline
value class Query(
    val params: List<Pair<String, Any>>,
) {
    constructor(vararg params: Pair<String, Any>) : this(params.asList())

    companion object {
        val Empty = Query(emptyList())
    }
}
