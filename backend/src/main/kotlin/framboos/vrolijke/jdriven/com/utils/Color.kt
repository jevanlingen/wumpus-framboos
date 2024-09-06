package framboos.vrolijke.jdriven.com.utils

import kotlin.random.Random

private fun randomColorHex() = Random.nextInt(255).toString(16).uppercase().padEnd(2, '0')

private val hexColorRegex = Regex("^[0-9A-Fa-f]{6}\$")

@JvmInline
value class Color private constructor(
    private val value: String,
) {
    companion object {
        fun random() = Color(randomColorHex() + randomColorHex() + randomColorHex())

        fun randomSkinColor() = Color(listOf("FFFFFF", "FFFF00", "FFE0C4", "EECFB4", "DEAB7F", "BE723C").random())

        operator fun invoke(value: String) =
            if (hexColorRegex.matches(value)) Color(value) else throw IllegalArgumentException("$value is not a valid color")
    }

    override fun toString() = value

    operator fun invoke() = value
}
