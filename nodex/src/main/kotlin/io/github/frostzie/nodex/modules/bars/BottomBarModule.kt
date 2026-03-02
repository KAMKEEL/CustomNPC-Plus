package io.github.frostzie.nodex.modules.bars

import io.github.frostzie.nodex.loader.fabric.ModVersion
import javafx.beans.property.SimpleStringProperty

object BottomBarModule {

    val cursorPositionProperty = SimpleStringProperty("")
    val encodingProperty = SimpleStringProperty("UTF-8")
    val ideVersionProperty = SimpleStringProperty("DataPack IDE v${ModVersion.current}")

    fun updateCursorPosition(line: Int, column: Int) {
        cursorPositionProperty.set("Ln $line, Col $column")
    }

    fun updateEncoding(encoding: String) {
        encodingProperty.set(encoding)
    }
}