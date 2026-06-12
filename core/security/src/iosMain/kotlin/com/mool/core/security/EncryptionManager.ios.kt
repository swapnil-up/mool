package com.mool.core.security

import platform.Foundation.*
import platform.Security.*

actual class EncryptionManager {
    actual fun encrypt(plaintext: ByteArray): ByteArray {
        return plaintext // Stub — Keychain encryption not yet implemented
    }

    actual fun decrypt(ciphertext: ByteArray): ByteArray {
        return ciphertext // Stub — Keychain decryption not yet implemented
    }
}
