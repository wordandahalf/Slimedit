package org.wordandahalf.slimedit.ui

import br.com.gamemods.nbtmanipulator.NbtTag
import javafx.event.EventHandler
import javafx.scene.control.TextInputDialog
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.stage.StageStyle

class TreeItemKeyEventHandler(private val treeView: TreeView<String>) : EventHandler<KeyEvent> {
    override fun handle(e: KeyEvent) {
        if(e.code == KeyCode.DELETE) {
            treeView.selectionModel.selectedItems.forEach {
                if(it is NbtTagTreeItem<*, *>) {
                    val nbtItem = it as NbtTagTreeItem<*, Any>

                    if(nbtItem.delete()) it.getParent().children.remove(it)
                }
            }
        }
    }
}

class TreeItemMouseEventHandler : EventHandler<MouseEvent> {
    override fun handle(e: MouseEvent) {
        if(e.clickCount == 2) {
            val node = e.pickResult.intersectedNode
            val cell = node.parent

            if(cell is TreeCell<*>) {
                val item = cell.treeItem

                if(item is EditableNbtTagTreeItem<*, *>) {
                    promptForNewValue(item as EditableNbtTagTreeItem<NbtTag, Any>)
                }
            }
        }
    }

    private fun promptForNewValue(item: EditableNbtTagTreeItem<NbtTag, Any>) {
        val dialog = TextInputDialog(item.displayedValue())
        dialog.initStyle(StageStyle.UTILITY)
        dialog.title = "Edit"
        dialog.contentText = "Enter a new ${item.friendlyTypeName}:"

        dialog.showAndWait().ifPresent {
            val newValue = item.toNbtValueType(it)

            if(newValue == null) {
                promptForNewValue(item)
            } else {
                item.set(newValue)
            }
        }
    }
}