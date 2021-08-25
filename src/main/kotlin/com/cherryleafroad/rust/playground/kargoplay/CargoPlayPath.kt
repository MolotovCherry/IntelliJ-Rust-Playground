package com.cherryleafroad.rust.playground.kargoplay

import com.cherryleafroad.rust.playground.runconfig.constants.CargoConstants
import com.cherryleafroad.rust.playground.runconfig.constants.CargoConstants.MANIFEST_FILE
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

private enum class TargetType(val folderName: String) {
    DEBUG("debug"),
    RELEASE("release")
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
class CargoPlayPath(
    private val srcs: List<String>,
    private val cwd: String
) {
    val srcHash = genSrcHash()
    val projectHash = "p${srcHash.lowercase()}"

    val cargoPlayDir = Paths.get(System.getProperty("java.io.tmpdir"), "cargo-play.$srcHash").toString()
    val cargoManifest = Paths.get(cargoPlayDir, MANIFEST_FILE).toString()
    val srcDir = Paths.get(cargoPlayDir, CargoConstants.ProjectLayout.source).toString()
    val targetDir = Paths.get(cargoPlayDir, CargoConstants.ProjectLayout.target).toString()
    val debugDir = Paths.get(targetDir, CargoConstants.ProjectLayout.debugDir).toString()
    val releaseDir = Paths.get(targetDir, CargoConstants.ProjectLayout.releaseDir).toString()
    val debugTarget = binaryTarget(TargetType.DEBUG).toString()
    val releaseTarget = binaryTarget(TargetType.RELEASE).toString()

    private fun binaryTarget(target: TargetType): Path {
        val os = System.getProperty("os.name").lowercase()
        val fileExt = if (os.contains("win")) ".exe" else ""

        return Paths.get(cargoPlayDir, "target/${target.folderName}/$projectHash$fileExt")
    }

    private fun genSrcHash(): String {
        val hash = MessageDigest.getInstance("SHA-1")
        val canonicalized = srcs.map {
            val f = Paths.get(it).toFile()

            val os = System.getProperty("os.name").lowercase()
            val prefix = if (os.contains("win")) "\\\\?\\" else ""

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
            INDEXES[ALPHABET[i].code] = i
        }
    }

    fun encode(input: ByteArray): String {
        var data = input
        if (data.isEmpty()) {
            return ""
        }
        // Count leading zeros.
        var zeros = 0
        while (zeros < data.size && data[zeros].toInt() == 0) {
            ++zeros
        }
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        data = data.copyOf(data.size) // since we modify it in-place
        val encoded = CharArray(data.size * 2) // upper bound
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < data.size) {
            val remainder = divmod(data, inputStart, 256, 58)
            encoded[--outputStart] = ALPHABET[remainder]
            if (data[inputStart].toInt() == 0) {
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
