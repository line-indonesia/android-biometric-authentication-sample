package com.linecorp.id.research.biometricdemo.data

data class EncryptedMessage(
    val cipherText: ByteArray,
    val initializationVector: ByteArray,
    val savedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedMessage
        if (!cipherText.contentEquals(other.cipherText)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cipherText.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}
