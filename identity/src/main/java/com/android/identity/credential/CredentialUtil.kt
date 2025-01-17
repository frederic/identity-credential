/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.identity.credential

import com.android.identity.securearea.CreateKeySettings
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Timestamp

/**
 * A set of utilities and helpers for working with credentials.
 */
object CredentialUtil {
    private const val TAG = "CredentialUtil"

    /**
     * A helper for managing a set of authentication keys.
     *
     * This helper provides a high-level way to manage authentication keys on a
     * [Credential]. Its goal is to always have a fixed number of authentication
     * keys available within the following constraints
     *
     * - If a key is used more than `maxUsesPerKey` times, a replacement is generated.
     * - If a key expires within `minValidTimeMillis` milliseconds, a replacement is generated.
     *
     * This is all implemented on top of [Credential.createPendingAuthenticationKey]
     * and [PendingAuthenticationKey.certify]. The application should examine the return
     * value and if positive, collect the pending authentication keys via
     * [Credential.pendingAuthenticationKeys], send them to the issuer for certification,
     * and then call [PendingAuthenticationKey.certify] when receiving the certification
     * from the issuer.
     *
     * @param credential the credential to manage authentication keys for.
     * @param secureArea the secure area to use for new pending authentication keys.
     * @param createKeySettings the settings used to create new pending authentication keys.
     * @param domain the domain to use for created authentication keys.
     * @param now the time right now, used for determining which existing keys to replace.
     * @param numAuthenticationKeys the number of authentication keys that should be kept.
     * @param maxUsesPerKey the maximum number of uses per key.
     * @param minValidTimeMillis requests a replacement for a key if it expires within this window.
     * @param dryRun don't actually create the keys, just return how many would be created.
     * @return the number of pending authentication keys created.
     */
    @JvmStatic
    fun managedAuthenticationKeyHelper(
        credential: Credential,
        secureArea: SecureArea,
        createKeySettings: CreateKeySettings?,
        domain: String,
        now: Timestamp,
        numAuthenticationKeys: Int,
        maxUsesPerKey: Int,
        minValidTimeMillis: Long,
        dryRun: Boolean
    ): Int {
        // First determine which of the existing keys need a replacement...
        var numKeysNotNeedingReplacement = 0
        var numReplacementsGenerated = 0
        for (authKey in credential.authenticationKeys.filter { it.domain == domain }) {
            var keyExceededUseCount = false
            var keyBeyondExpirationDate = false
            if (authKey.usageCount >= maxUsesPerKey) {
                keyExceededUseCount = true
            }
            val expirationDate = Timestamp.ofEpochMilli(
                authKey.validUntil.toEpochMilli() - minValidTimeMillis
            )
            if (now.toEpochMilli() > expirationDate.toEpochMilli()) {
                keyBeyondExpirationDate = true
            }
            if (keyExceededUseCount || keyBeyondExpirationDate) {
                if (authKey.replacement == null) {
                    if (!dryRun) {
                        credential.createPendingAuthenticationKey(
                            domain,
                            secureArea,
                            createKeySettings!!,
                            authKey
                        )
                    }
                    numReplacementsGenerated++
                    continue
                }
            }
            numKeysNotNeedingReplacement++
        }

        val numExistingPendingKeys =
            credential.pendingAuthenticationKeys.filter { it.domain == domain }.size

        // It's possible we need to generate pending keys that aren't replacements
        val numNonReplacementsToGenerate = (numAuthenticationKeys
                - numKeysNotNeedingReplacement
                - numExistingPendingKeys)
        if (!dryRun) {
            if (numNonReplacementsToGenerate > 0) {
                for (n in 0 until numNonReplacementsToGenerate) {
                    val pendingKey = credential.createPendingAuthenticationKey(
                        domain,
                        secureArea,
                        createKeySettings!!,
                        null
                    )
                    pendingKey.applicationData.setBoolean(domain, true)
                }
            }
        }
        return numReplacementsGenerated + numNonReplacementsToGenerate
    }
}