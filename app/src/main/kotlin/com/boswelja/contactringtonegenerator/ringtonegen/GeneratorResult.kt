package com.boswelja.contactringtonegenerator.ringtonegen

enum class GeneratorResult {
    FAILED,
    MIXED,
    SUCCESSFUL
}

/**
 * A data class containing two sets of
 * [com.boswelja.contactringtonegenerator.contactpicker.Contact.lookupKey]s. One for contacts whose
 * ringtone was successfully generated, and one for contacts whose ringtone failed to generate.
 * @param successfulContacts The [Set] of lookupKeys for contacts whose ringtone was generated.
 * @param failedContacts The [Set] of lookupKeys for contacts whose ringtone was not generated.
 */
data class GeneratorResultData(
    val successfulContacts: Set<String>,
    val failedContacts: Set<String>
)
