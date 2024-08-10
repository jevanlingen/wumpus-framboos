fun parseJsonObject(json: String): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    val content = json.trim().removeSurrounding("{", "}")

    val pairs = content.split(",").map { it.trim() }
    for (pair in pairs) {
        val (key, value) = pair.split(":").map { it.trim().removeSurrounding("\"") }
        result[key] = parseJsonValue(value)
    }

    return result
}

fun parseJsonArray(json: String): List<Any?> {
    val content = json.trim().removeSurrounding("[", "]")
    return content.split(",").map { parseJsonValue(it.trim()) }
}

fun parseJsonValue(value: String): Any? =
    when {
        value.startsWith("\"") -> value.removeSurrounding("\"") // String
        value == "true" -> true // Boolean
        value == "false" -> false // Boolean
        value == "null" -> null // Null
        value.contains(".") -> value.toDoubleOrNull() // Double
        value.toIntOrNull() != null -> value.toInt() // Integer
        value.startsWith("{") -> parseJsonObject(value) // Nested JSON Object
        value.startsWith("[") -> parseJsonArray(value) // JSON Array
        else -> value // Fallback to string
    }
