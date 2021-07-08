package org.wordandahalf.slimedit.ui

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TreeView
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import org.wordandahalf.slimedit.world.InvalidWorldException
import tornadofx.View
import org.wordandahalf.slimedit.world.SlimeWorld

class SlimeditView : View() {
    private var world : SlimeWorld? = null

    override val root : BorderPane by fxml("/main.fxml")
    private val treeView: TreeView<String> by fxid()

    init {
        treeView.isEditable = true
        treeView.addEventFilter(KeyEvent.ANY, TreeItemKeyEventHandler(treeView))
        treeView.addEventFilter(MouseEvent.MOUSE_CLICKED, TreeItemMouseEventHandler())
    }

    @FXML
    fun openItemClicked() {
        val chooser = FileChooser()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Slime world", "*.slime"))

        val file = chooser.showOpenDialog(this.currentWindow)
        if(file != null) {
            world = SlimeWorld(file)
            try {
                world!!.load()
            } catch (e: InvalidWorldException) {
                Alert(Alert.AlertType.ERROR, e.toString(), ButtonType.OK).showAndWait()
                world = null
                return
            }

            treeView.root = SlimeWorldTreeItem(world!!)
        }
    }

    @FXML
    fun saveItemClicked() {
        try {
            world?.save()
            Alert(Alert.AlertType.INFORMATION, "World ${world?.name} has been saved.").showAndWait()
        } catch (e: Exception) {
            Alert(Alert.AlertType.ERROR, e.toString(), ButtonType.OK).showAndWait()
        }
    }

    @FXML
    fun closeItemClicked() {
        this.close()
    }

    @FXML
    fun deleteItemClicked() {
        treeView.selectionModel.selectedItems.forEach {
            if(it is NbtTagTreeItem<*, *>) {
                val nbtItem = it as NbtTagTreeItem<*, Any>

                if(nbtItem.delete()) it.getParent().children.remove(it)
            }
        }
    }

    @FXML
    fun aboutItemClicked() {
        val about = Alert(Alert.AlertType.NONE, "Something, something, something.\n\nCopyright (c) Ryan Jones. Licensed under the terms of the MIT License.", ButtonType.CLOSE)
        about.title = "About Slimedit"

        about.show()
    }
}