package io.github.frostzie.nodex.screen.elements.project

import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.project.Project
import io.github.frostzie.nodex.utils.ProjectIconUtils
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL

class RecentProjectListCell : ListCell<Project>() {

    init {
        styleClass.add("recent-project-cell")
    }

    override fun updateItem(item: Project?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            tooltip = null
        } else {
            val iconNode = if (item.additionalPaths.isNotEmpty()) {
                FontIcon(Material2AL.FOLDER).apply { iconSize = 32 }
            } else {
                ProjectIconUtils.getIcon(item.path, 32.0)
            }

            val nameLabel = Label(item.name)
            nameLabel.styleClass.add(Styles.TEXT_BOLD)
            nameLabel.maxWidth = Double.MAX_VALUE

            val contentBox = VBox(2.0)
            HBox.setHgrow(contentBox, Priority.ALWAYS)
            contentBox.children.add(nameLabel)

            val metadata = item.metadata
            if (metadata != null) {
                val fullDesc = metadata.description.trim()
                if (fullDesc.isNotBlank()) {
                    // Logic: Row 1 = First line, Row 2 = Rest joined by space
                    val lines = fullDesc.split("\n")
                    val header = lines[0].trim()
                    val rest = lines.drop(1).joinToString(" ").trim()

                    if (header.isNotBlank()) {
                        val headerLabel = Label(header.take(60).let { if (it.length == 60) "$it..." else it })
                        headerLabel.styleClass.add(Styles.TEXT_SMALL)
                        headerLabel.maxWidth = Double.MAX_VALUE
                        contentBox.children.add(headerLabel)
                    }

                    if (rest.isNotBlank()) {
                        val restLabel = Label(rest.take(60).let { if (it.length == 60) "$it..." else it })
                        restLabel.styleClass.addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED)
                        restLabel.maxWidth = Double.MAX_VALUE
                        contentBox.children.add(restLabel)
                    }
                }
            } else {
                val pathLabel = Label(item.path.toString())
                pathLabel.styleClass.addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED)
                pathLabel.maxWidth = Double.MAX_VALUE
                contentBox.children.add(pathLabel)
            }

            val root = HBox(15.0)
            root.alignment = Pos.CENTER_LEFT
            root.minHeight = 60.0
            root.prefHeight = 60.0
            root.maxHeight = 60.0
            root.children.addAll(iconNode, contentBox)
            
            graphic = root
            text = null
        }
    }
}