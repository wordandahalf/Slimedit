package org.wordandahalf.slimedit.world

import br.com.gamemods.nbtmanipulator.*
import com.github.luben.zstd.Zstd
import org.wordandahalf.slimedit.toTag
import java.io.*
import java.lang.StringBuilder
import kotlin.math.ceil

class InvalidWorldException(message: String) : IOException(message)

class SlimeWorld(val file: File) {
    data class Data(
        val worldVersion: Byte,
        val minX: Short,
        val minZ: Short,
        val width: Int,
        val depth: Int,
        val chunkBitmask: ByteArray,
        val uncompressedChunkSize: Int,
        val compressedChunkData: ByteArray,
        val tileEntities: NbtCompound,
        val hasEntities: Boolean,
        val entities: NbtCompound,
        val extra: NbtCompound,
        val uncompressedMapsSize: Int,
        val compressedMaps: ByteArray
    ) {
        override fun toString(): String {
            val builder = StringBuilder()

            builder.appendLine("SlimeWorldData {")
            builder.appendLine("\tworldVersion: $worldVersion")
            builder.appendLine("\tminX: $minX")
            builder.appendLine("\tminZ: $minZ")
            builder.appendLine("\twidth: $width")
            builder.appendLine("\tdepth: $depth")
            builder.appendLine("\tchunkBitmask (${chunkBitmask.size} bytes): {${chunkBitmask.joinToString { Integer.toHexString(it.toInt() and 0xFF) }}}")
            builder.appendLine("\tchunks: ${compressedChunkData.size} bytes compressed, $uncompressedChunkSize bytes uncompressed")
            builder.appendLine("\ttileEntities: ${tileEntities::class.simpleName} with ${tileEntities.size} entries")
            builder.appendLine("\thasEntities: $hasEntities")
            if(hasEntities)
                builder.appendLine("\tentities: ${entities::class.simpleName} with ${entities.size} entries")
            builder.appendLine("\textra: ${extra::class.simpleName} with ${extra.size} entries")
            builder.appendLine("\tworldMaps: ${compressedMaps.size} bytes compressed, $uncompressedMapsSize bytes uncompressed")
            builder.appendLine("}")
            
            return builder.toString()
        }
    }

    val name = file.nameWithoutExtension
    lateinit var data: Data

    private object Constants {
        val Header = byteArrayOf(0xB1.toByte(), 0x0B)
        const val Version : Byte = 0x09
    }

    fun load() {
        if(this::data.isInitialized) return

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

        // Read compressed chunk data
        val compressedChunksSize = stream.readInt()
        val uncompressedChunksSize = stream.readInt()

        val compressedChunkData = stream.readNBytes(compressedChunksSize)

        // Read tile entity data
        val tileEntities = readCompressedTag(stream, stream.readInt(), stream.readInt()) ?: NbtCompound(mapOf(Pair("tiles", NbtList<NbtCompound>())))

        // Read entity data
        val hasEntities = stream.readBoolean()
        var entities = NbtCompound(mapOf(Pair("entities", NbtList<NbtCompound>())))
        if(hasEntities)
            readCompressedTag(stream, stream.readInt(), stream.readInt())?.also { entities = it }

        val extra = readCompressedTag(stream, stream.readInt(), stream.readInt())!!

        val compressedWorldMapsSize = stream.readInt()
        val uncompressedWorldMapsSize = stream.readInt()
        val worldMapsData = stream.readNBytes(compressedWorldMapsSize)

        stream.close()

        this.data = Data(
            worldVersion, minX, minZ, width, depth, chunkBitmask, uncompressedChunksSize, compressedChunkData, tileEntities, hasEntities, entities, extra, uncompressedWorldMapsSize, worldMapsData
        )
    }

    fun save() {
        val stream = DataOutputStream(File(file.parent, file.nameWithoutExtension + "_edited.slime").outputStream())

        stream.write(Constants.Header)
        stream.writeByte(Constants.Version.toInt())
        stream.writeByte(data.worldVersion.toInt())

        stream.writeShort(data.minX.toInt())
        stream.writeShort(data.minZ.toInt())
        stream.writeShort(data.width)
        stream.writeShort(data.depth)

        stream.write(data.chunkBitmask)

        stream.writeInt(data.compressedChunkData.size)
        stream.writeInt(data.uncompressedChunkSize)
        stream.write(data.compressedChunkData)

        // Write tile entities data
        var serializedData = serializeCompressedTag(data.tileEntities)
        stream.writeInt(serializedData.second.size)
        stream.writeInt(serializedData.first)
        stream.write(serializedData.second)

        // Write entities data
        stream.writeBoolean(data.hasEntities)
        if(data.hasEntities) {
            serializedData = serializeCompressedTag(data.entities)
            stream.writeInt(serializedData.second.size)
            stream.writeInt(serializedData.first)
            stream.write(serializedData.second)
        }

        // Write extra data
        serializedData = serializeCompressedTag(data.extra)
        stream.writeInt(serializedData.second.size)
        stream.writeInt(serializedData.first)
        stream.write(serializedData.second)

        // Write world maps data
        stream.writeInt(data.compressedMaps.size)
        stream.writeInt(data.uncompressedMapsSize)
        stream.write(data.compressedMaps)

        stream.flush()
        stream.close()
    }

    fun getTileEntityData() = data.tileEntities["tiles"] as NbtList<NbtCompound>
    fun getEntityData() = data.entities["entities"] as NbtList<NbtCompound>

    private fun readCompressedTag(stream: DataInputStream, compressedSize: Int, uncompressedSize: Int) : NbtCompound? {
        val compressedData = stream.readNBytes(compressedSize)
        val uncompressedData = ByteArray(uncompressedSize)

        Zstd.decompress(uncompressedData, compressedData)

        return uncompressedData.toTag()
    }

    private fun serializeCompressedTag(nbt: NbtTag) : Pair<Int, ByteArray> {
        val stream = ByteArrayOutputStream()
        NbtIO.writeNbtFile(stream, NbtFile("", nbt), false)
        val uncompressedData = stream.toByteArray()
        val uncompressedLength = uncompressedData.size
        stream.close()

        return Pair(uncompressedLength, Zstd.compress(uncompressedData))
    }

    override fun toString() = name
}