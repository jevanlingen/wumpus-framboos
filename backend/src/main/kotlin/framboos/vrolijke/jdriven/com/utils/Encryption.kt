package framboos.vrolijke.jdriven.com.utils

import org.mindrot.jbcrypt.BCrypt

fun checkPassword(plaintext: String?, hashed: String?) =
    hashed?.let { BCrypt.checkpw(plaintext, hashed) } ?: false

fun hashPassword(password: String?) =
    BCrypt.hashpw(password, BCrypt.gensalt())
