package org.wordandahalf.slimedit

import br.com.gamemods.nbtmanipulator.NbtCompound
import br.com.gamemods.nbtmanipulator.NbtIO

fun ByteArray.toTag() : NbtCompound? {
    if(this.isEmpty()) return null

    val stream = NbtIO.readNbtFile(this.inputStream(), false)
    val tag = stream.tag

    return tag as NbtCompound
}