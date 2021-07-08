package org.wordandahalf.slimedit.ui

import br.com.gamemods.nbtmanipulator.*
import javafx.scene.control.TreeItem
import org.wordandahalf.slimedit.bean.ObservableProperty
import org.wordandahalf.slimedit.world.SlimeWorld
import java.lang.NumberFormatException
import java.security.InvalidParameterException
import kotlin.experimental.and

class SlimeWorldTreeItem(world: SlimeWorld) : TreeItem<String>(world.name) {
    private val entityItem = ListTagTreeItem("Entities (${world.getEntityData().size})", world.getEntityData(), null)
    private val tileEntityItem = ListTagTreeItem("Tile Entities (${world.getTileEntityData().size})", world.getTileEntityData(), null)
    private val settingsItem = CompoundTagTreeItem("Settings (${world.data.extra.size})", world.data.extra, null)

    init {
        children.addAll(entityItem, tileEntityItem, settingsItem)
    }
}

/**
 * Wrapper class that allows [NbtTag]s to be stored in a [javafx.scene.control.TreeView]
 */
abstract class NbtTagTreeItem<TagType : NbtTag, ValueType : Any>(val name: String, val nbt: TagType, val parent: NbtTag?) : TreeItem<String>() {
    protected val textProperty = ObservableProperty { displayedText() }

    init {
        valueProperty().bind(textProperty)
    }

    fun delete() : Boolean {
        if(parent == null) return false

        if(parent is NbtList<*>) {
            parent.remove(nbt)
            return true
        }
        else if(parent is NbtCompound) {
            parent.remove(name, nbt)
            return true
        }

        return false
    }

    open fun displayedText() = name

    protected companion object {
        private val NbtTypeTreeItemMap = mapOf(
            Pair(NbtByte::class, ::ByteTagTreeItem),
            Pair(NbtShort::class, ::ShortTagTreeItem),
            Pair(NbtInt::class, ::IntTagTreeItem),
            Pair(NbtLong::class, ::LongTagTreeItem),
            Pair(NbtFloat::class, ::FloatTagTreeItem),
            Pair(NbtDouble::class, ::DoubleTagTreeItem),
            Pair(NbtByteArray::class, ::ByteArrayTagTreeItem),
            Pair(NbtString::class, ::StringTagTreeItem),
            Pair(NbtList::class, ::ListTagTreeItem),
            Pair(NbtCompound::class, ::CompoundTagTreeItem),
            Pair(NbtIntArray::class, ::IntArrayTagTreeItem),
        )

        fun makeTreeItem(name: String, nbt: NbtTag, parent: NbtTag) : NbtTagTreeItem<*, *> {
            val type = nbt::class
            val constructor = NbtTypeTreeItemMap[type] ?: throw InvalidParameterException("Unrecognized TagType '${type.simpleName}'!")

            return constructor.call(name, nbt, parent)
        }
    }
}

abstract class EditableNbtTagTreeItem<TagType : NbtTag, ValueType : Any>
    (name: String, nbt: TagType, parent: NbtTag, val friendlyTypeName: String)
    : NbtTagTreeItem<TagType, ValueType>(name, nbt, parent) {

    init {
        textProperty.invalidate()
    }

    fun get() = getNbtValue()

    fun set(value: ValueType) {
        setNbtValue(value)
        textProperty.invalidate()
    }

    protected abstract fun getNbtValue(): ValueType
    protected abstract fun setNbtValue(value: ValueType)

    /**
     * Method for converting user-inputted strings to the tag's value's type.
     * @return null if the provided string cannot be converted to the value's type, the converted value otherwise.
     */
    abstract fun toNbtValueType(value: String): ValueType?
    override fun displayedText() = "($friendlyTypeName) $name: ${displayedValue()}"
    abstract fun displayedValue() : String
}

class ByteTagTreeItem(name: String, nbt: NbtByte, parent: NbtTag) : EditableNbtTagTreeItem<NbtByte, Byte>(name, nbt, parent, "Byte") {
    override fun toNbtValueType(value: String): Byte? {
        return try {
            value.toByte()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: Byte) { nbt.value = value }
    override fun displayedValue(): String = (nbt.value and 0xFF.toByte()).toString()
}

class ShortTagTreeItem(name: String, nbt: NbtShort, parent: NbtTag) : EditableNbtTagTreeItem<NbtShort, Short>(name, nbt, parent, "Short") {
    override fun toNbtValueType(value: String): Short? {
        return try {
            value.toShort()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: Short) { nbt.value = value }
    override fun displayedValue(): String = (nbt.value and 0xFFFF.toShort()).toString()
}

class IntTagTreeItem(name: String, nbt: NbtInt, parent: NbtTag) : EditableNbtTagTreeItem<NbtInt, Int>(name, nbt, parent, "Int") {
    override fun toNbtValueType(value: String): Int? {
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: Int) { nbt.value = value }
    override fun displayedValue(): String = nbt.value.toString()
}

class LongTagTreeItem(name: String, nbt: NbtLong, parent: NbtTag) : EditableNbtTagTreeItem<NbtLong, Long>(name, nbt, parent, "Long") {
    override fun toNbtValueType(value: String): Long? {
        return try {
            value.toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: Long) { nbt.value = value }
    override fun displayedValue(): String = nbt.value.toString()
}

class FloatTagTreeItem(name: String, nbt: NbtFloat, parent: NbtTag) : EditableNbtTagTreeItem<NbtFloat, Float>(name, nbt, parent, "Float") {
    override fun toNbtValueType(value: String): Float? {
        return try {
            value.toFloat()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: Float) { nbt.value = value }
    override fun displayedValue(): String = nbt.value.toString()
}

class DoubleTagTreeItem(name: String, nbt: NbtDouble, parent: NbtTag) : EditableNbtTagTreeItem<NbtDouble, Double>(name, nbt, parent, "Double") {
    override fun toNbtValueType(value: String): Double? {
        return try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: Double) { nbt.value = value }
    override fun displayedValue(): String = nbt.value.toString()
}

class ByteArrayTagTreeItem(name: String, nbt: NbtByteArray, parent: NbtTag) : NbtTagTreeItem<NbtByteArray, ByteArray>(name, nbt, parent) {
    init {
        children.addAll(nbt.value.mapIndexed { i, byte -> ByteTagTreeItem("[$i]", NbtByte(byte), nbt) })
    }
}

class StringTagTreeItem(name: String, nbt: NbtString, parent: NbtTag) : EditableNbtTagTreeItem<NbtString, String>(name, nbt, parent, "String") {
    override fun toNbtValueType(value: String): String {
        return value
    }

    override fun getNbtValue() = nbt.value
    override fun setNbtValue(value: String) { nbt.value = value }
    override fun displayedValue(): String = nbt.value
}

class ListTagTreeItem(name: String, nbt: NbtList<NbtCompound>, parent: NbtTag?) : NbtTagTreeItem<NbtList<NbtCompound>, MutableList<NbtCompound>>(name, nbt, parent) {
    init {
        if(nbt.isNotEmpty()) {
            children.addAll(
                nbt.mapIndexed { i, tag: NbtTag ->
                    makeTreeItem("[$i]", tag, nbt)
                }
            )
        }
    }
}

class CompoundTagTreeItem(name: String, nbt: NbtCompound, parent: NbtTag?) : NbtTagTreeItem<NbtCompound, LinkedHashMap<String, NbtTag>>(name, nbt, parent) {
    init {
        if(nbt.isNotEmpty()) {
            children.addAll(
                nbt.map { entry: Map.Entry<String, NbtTag> ->
                    makeTreeItem(entry.key, entry.value, nbt)
                }
            )
        }
    }
}

class IntArrayTagTreeItem(name: String, nbt: NbtIntArray, parent: NbtTag) : NbtTagTreeItem<NbtIntArray, IntArray>(name, nbt, parent) {
    init {
        children.addAll(nbt.value.mapIndexed { i, int -> IntTagTreeItem("[$i]", NbtInt(int), parent) })
    }
}