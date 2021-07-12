package com.boswelja.contactringtonegenerator.ringtonegen.generator

import android.net.Uri
import com.boswelja.contactringtonegenerator.ringtonebuilder.ContactDataItem
import com.boswelja.contactringtonegenerator.ringtonebuilder.CustomAudioItem
import com.boswelja.contactringtonegenerator.ringtonebuilder.CustomTextItem
import com.boswelja.contactringtonegenerator.ringtonebuilder.StructureItem

/**
 * A minimal class containing only the required data for the generator to interpret.
 */
internal sealed class Block

/**
 * A block containing text data to be operated on.
 */
internal class TextBlock(
    val text: String
) : Block()

/**
 * A block containing a file Uri to operate on.
 */
internal class FileBlock(
    val uri: Uri
) : Block()

/**
 * Convert a list of [StructureItem] to a list of [Block].
 */
internal fun List<StructureItem>.toBlocks(): List<Block> {
    val workingText = mutableListOf<String>()
    val blocks = mutableListOf<Block>()
    forEach { item ->
        when (item) {
            is ContactDataItem,
            is CustomTextItem -> {
                // Collect text for future use.
                workingText.add(item.data!!.trim())
            }
            is CustomAudioItem -> {
                // Add text from last items if needed
                if (workingText.isNotEmpty()) {
                    blocks.add(TextBlock(workingText.joinToString(separator = " ")))
                    workingText.clear()
                }

                // Add the audio item
                blocks.add(FileBlock(item.audioUri!!))
            }
        }
    }
    if (workingText.isNotEmpty()) {
        blocks.add(TextBlock(workingText.joinToString(separator = " ")))
    }

    return blocks
}
