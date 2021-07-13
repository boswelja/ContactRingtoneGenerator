package com.boswelja.contactringtonegenerator.ringtonegen

import kotlinx.serialization.Serializable

/**
 * A data class containing two sets of
 * [com.boswelja.contactringtonegenerator.contactpicker.Contact.lookupKey]s. One for contacts whose
 * ringtone was successfully generated, and one for contacts whose ringtone failed to generate.
 * @param successfulContacts The [Set] of lookupKeys for contacts whose ringtone was generated.
 * @param failedContacts The [Set] of lookupKeys for contacts whose ringtone was not generated.
 */
@Serializable
data class GeneratorResult(
    val successfulContacts: Set<String> = emptySet(),
    val failedContacts: Set<String> = emptySet()
) {
    val hasSuccesses = successfulContacts.isNotEmpty()
    val hasFails = failedContacts.isNotEmpty()
}
