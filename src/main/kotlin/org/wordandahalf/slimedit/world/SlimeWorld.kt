package org.wordandahalf.slimedit.world

import br.com.gamemods.nbtmanipulator.NbtCompound
import br.com.gamemods.nbtmanipulator.NbtList
import com.github.luben.zstd.Zstd
import org.wordandahalf.slimedit.toTag
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import kotlin.math.ceil
import kotlin.properties.Delegates

class InvalidWorldException(message: String) : IOException(message)

class SlimeWorld(val file: File) {
    val name = file.nameWithoutExtension
    var formatVersion by Delegates.notNull<Byte>(); private set
    var worldVersion by Delegates.notNull<Byte>(); private set

    lateinit var tileEntities: NbtList<NbtCompound>; private set
    lateinit var entities: NbtList<NbtCompound>; private set
    lateinit var extra: NbtCompound; private set

    private object Constants {
        val Header = byteArrayOf(0xB1.toByte(), 0x0B)
        const val Version : Byte = 0x09
    }

    fun load() {
        val stream = DataInputStream(file.inputStream())

        val header = stream.readNBytes(2)
        if(!header.contentEquals(Constants.Header)) throw InvalidWorldException("World does not have the SRF header!")

        val fileVersion = stream.readByte()
        if(fileVersion != Constants.Version) throw InvalidWorldException("World is not a supported version!")

        val worldVersion = stream.readByte()

        val minX = stream.readShort()
        val minZ = stream.readShort()
        val width = stream.readUnsignedShort()
        val depth = stream.readUnsignedShort()

        val chunkBitmask = stream.readNBytes(ceil((width * depth) / 8.0).toInt())
        val compressedChunksSize = stream.readInt()
        val uncompressedChunksSize = stream.readInt()

        println("Chunks are ${compressedChunksSize / 1024} KB compressed, ${uncompressedChunksSize / 1024} KB uncompressed.")
        val compressedChunkData = stream.readNBytes(compressedChunksSize)

        val tileEntities = readCompressedTag(stream, stream.readInt(), stream.readInt())

        val hasEntities = stream.readBoolean()
        var entities : NbtCompound? = null

        if(hasEntities) entities = readCompressedTag(stream, stream.readInt(), stream.readInt())

        this.extra = readCompressedTag(stream, stream.readInt(), stream.readInt())!!

        val compressedWorldMapsSize = stream.readInt()
        val uncompressedWorldMapsSize = stream.readInt()
        val worldMapsData = stream.readNBytes(compressedWorldMapsSize)

        this.tileEntities = tileEntities?.get("tiles") as NbtList<NbtCompound>
        this.entities = entities?.get("entities") as NbtList<NbtCompound>

        stream.close()
    }

    fun save() {

    }

    private fun readCompressedTag(stream: DataInputStream, compressedSize: Int, uncompressedSize: Int) : NbtCompound? {
        val compressedData = stream.readNBytes(compressedSize)
        val uncompressedData = ByteArray(uncompressedSize)

        Zstd.decompress(uncompressedData, compressedData)

        return uncompressedData.toTag()
    }

    override fun toString() = name
}