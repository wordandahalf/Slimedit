package org.wordandahalf.slimedit.bean

import javafx.beans.binding.ObjectBinding

class ObservableProperty<T>(private val f: () -> T) : ObjectBinding<T>() {
    override fun computeValue(): T = f()
}