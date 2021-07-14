package com.boswelja.contactringtonegenerator.common.contacts

/**
 * Represents the full name for a [Contact].
 * @param prefix See [android.provider.ContactsContract.CommonDataKinds.StructuredName.PREFIX].
 * @param givenName See [android.provider.ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME].
 * @param middleName See [android.provider.ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME].
 * @param familyName See [android.provider.ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME].
 * @param suffix See [android.provider.ContactsContract.CommonDataKinds.StructuredName.SUFFIX].
 */
data class StructuredName(
    val prefix: String = "",
    val givenName: String = "",
    val middleName: String = "",
    val familyName: String = "",
    val suffix: String = ""
)
