fun parseJsonObject(json: String): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    val content = json.trim().removeSurrounding("{", "}")

    val pairs = splitJson(content, ',')
    for (pair in pairs) {
        val keyValue = splitJson(pair, ':')
        val key = keyValue[0].trim().removeSurrounding("\"")
        val value = keyValue[1].trim()
        result[key] = parseJsonValue(value)
    }

    return result
}

fun parseJsonArray(json: String): List<Any?> {
    val content = json.trim().removeSurrounding("[", "]")
    val items = splitJson(content, ',')
    return items.map { parseJsonValue(it.trim()) }
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

fun splitJson(
    content: String,
    delimiter: Char,
): List<String> {
    val result = mutableListOf<String>()
    var depth = 0
    var current = StringBuilder()

    for (char in content) {
        when (char) {
            '{', '[' -> depth++
            '}', ']' -> depth--
        }

        if (char == delimiter && depth == 0) {
            result.add(current.toString())
            current = StringBuilder()
        } else {
            current.append(char)
        }
    }

    if (current.isNotEmpty()) {
        result.add(current.toString())
    }

    return result
}
