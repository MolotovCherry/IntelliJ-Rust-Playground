package com.cherryleafroad.rust.playground.utils

import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

private enum class TargetType(val folderName: String) {
    DEBUG("debug"),
    RELEASE("release")
}

class CargoPlayPath(
    private val srcs: List<String>,
    private val cwd: String
) {
    private val hash = getSrcHash()

    val cargoPlayDir: Path = Paths.get(System.getProperty("java.io.tmpdir"), "cargo-play.$hash")
    val debugTarget = binaryTarget(TargetType.DEBUG)
    val releaseTarget = binaryTarget(TargetType.RELEASE)

    private fun binaryTarget(target: TargetType): Path {
        val hashl = hash.toLowerCase()

        val os = System.getProperty("os.name").toLowerCase()
        val fileExt = if (os.contains("win")) {
            ".exe"
        } else {
            ""
        }

        return Paths.get(cargoPlayDir.toString(), "target/${target.folderName}/p$hashl$fileExt")
    }

    private fun getSrcHash(): String {
        val hash = MessageDigest.getInstance("SHA-1")
        val canonicalized = srcs.map {
            val f = Paths.get(it).toFile()

            val os = System.getProperty("os.name").toLowerCase()
            val prefix = if (os.contains("win")) {
                "\\\\?\\"
            } else {
                ""
            }

            if (!f.isAbsolute) {
                "$prefix${Paths.get(cwd, it).toFile().canonicalPath}"
            } else {
                "$prefix${f.canonicalPath}"
            }
        }

        val sorted = canonicalized.sorted()

        for (src in sorted) {
            hash.update(src.toByteArray(Charsets.UTF_8))
        }

        val digest = hash.digest()
        return Base58.encode(digest)
    }
}


private object Base58 {
    private val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val ENCODED_ZERO = ALPHABET[0]
    private val INDEXES = IntArray(128)

    init {
        Arrays.fill(INDEXES, -1)
        for (i in ALPHABET.indices) {
            INDEXES[ALPHABET[i].toInt()] = i // FIXME .toInt()
        }
    }

    fun encode(input: ByteArray): String {
        var input = input
        if (input.isEmpty()) {
            return ""
        }
        // Count leading zeros.
        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) {
            ++zeros
        }
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        input = input.copyOf(input.size) // since we modify it in-place
        val encoded = CharArray(input.size * 2) // upper bound
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < input.size) {
            val remainder = divmod(input, inputStart, 256, 58)
            encoded[--outputStart] = ALPHABET[remainder]
            if (input[inputStart].toInt() == 0) {
                ++inputStart // optimization - skip leading zeros
            }
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO
        }
        // Return encoded string (including encoded leading zeros).
        return String(encoded, outputStart, encoded.size - outputStart)
    }

    private fun divmod(number: ByteArray, firstDigit: Int, base: Int, divisor: Int): Int {
        // this is just long division which accounts for the base of the input digits
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toUByte() and 0xFF.toUByte() // Signed bytes are annoying kotlin! This can probably be improved.
            val temp = remainder * base + digit.toInt()
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder
    }
}
