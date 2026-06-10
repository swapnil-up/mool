package com.mool.core.security

import platform.Foundation.*
import platform.Security.*

actual class EncryptionManager {
    actual fun encrypt(plaintext: ByteArray): ByteArray {
        return plaintext // TODO: implement Keychain-based encryption
    }

    actual fun decrypt(ciphertext: ByteArray): ByteArray {
        return ciphertext // TODO: implement Keychain-based decryption
    }
}
