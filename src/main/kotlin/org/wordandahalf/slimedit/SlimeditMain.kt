package org.wordandahalf.slimedit

import tornadofx.App
import tornadofx.launch
import org.wordandahalf.slimedit.ui.SlimeditView
import org.wordandahalf.slimedit.world.SlimeWorld
import java.io.File

class SlimeditApp : App(SlimeditView::class)

fun main(args: Array<String>) {
    launch<SlimeditApp>(args)
}