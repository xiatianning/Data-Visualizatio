import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class Controller(var model: Model): VBox() {

    // Data set selector
    private val datasetSelector = ChoiceBox(model.datasetList).apply {
        prefWidth = 150.0
        prefHeight = 28.0
        selectionModel.select(0)
        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            val isNegativeBefore = model.isNegative()
            model.changeDataset(newValue)
            lineButton.isSelected = true
            changeDisable(isNegativeBefore)
        }
    }

    // Data set creator
    private val datasetText = TextField().apply {
        prefWidth = 150.0
        prefHeight = 28.0
        promptText = "Data set name"
    }
    private val createButton = Button("Create").apply {
        prefHeight = 28.0
        onAction = EventHandler {
            model.addDataset(datasetText.text)
            datasetText.text = ""
            datasetSelector.selectionModel.select(model.datasetList.size - 1)
        }
    }
    private val datasetCreator = HBox(datasetText, createButton)

    // Visualization selector
    private val lineButton = ToggleButton("Line").apply {
        prefWidth = 72.0
        prefHeight = 28.0
        userData = "line graph"
        isSelected = true
    }
    private val barButton = ToggleButton("Bar").apply {
        prefWidth = 72.0
        prefHeight = 28.0
        userData = "bar chart"
    }
    private val barSEMButton = ToggleButton("Bar (SEM)").apply {
        prefWidth = 72.0
        prefHeight = 28.0
        userData = "SEM bar chart"
    }
    private val pieButton = ToggleButton("Pie").apply {
        prefWidth = 72.0
        prefHeight = 28.0
        userData = "pie chart"
    }
    private val boxButton = ToggleButton("Box").apply {
        prefWidth = 72.0
        prefHeight = 28.0
        userData = "box plot"
    }
    private val visualizationSelector = HBox(lineButton, barButton, barSEMButton, pieButton, boxButton)
    private val visualizationGroup = ToggleGroup().apply {
        lineButton.toggleGroup = this
        barButton.toggleGroup = this
        barSEMButton.toggleGroup = this
        pieButton.toggleGroup = this
        boxButton.toggleGroup = this
    }

    // Color scheme selector
    private val rainbowButton = RadioButton("Rainbow").apply { userData = "rainbow"; isSelected = true}
    private val grayButton = RadioButton("Gradient gray").apply { userData = "gray" }
    private val lightButton = RadioButton("Light colors").apply { userData = "light" }
    private val colorSelector = HBox(
        Label("Color scheme:"),
        rainbowButton,
        grayButton,
        lightButton
    )
    private val colorGroup = ToggleGroup().apply {
        rainbowButton.toggleGroup = this
        grayButton.toggleGroup = this
        lightButton.toggleGroup = this
    }

    init {
        // Add children to the toolbar
        children.addAll(
            HBox(
                datasetSelector,
                Separator().apply {
                    orientation = Orientation.VERTICAL
                    padding = Insets(0.0, 15.0, 0.0, 15.0)
                },
                datasetCreator,
                Separator().apply {
                    orientation = Orientation.VERTICAL
                    padding = Insets(0.0, 15.0, 0.0, 15.0)
                },
                visualizationSelector
            ),
            Separator().apply {
                orientation = Orientation.HORIZONTAL
            },
            colorSelector.apply {
                spacing = 10.0
                padding = Insets(5.0, 0.0, 5.0, 0.0)
            }
        )

        // Add listener to visualizationGroup
        visualizationGroup.selectedToggleProperty().addListener { _, oldValue, newValue ->
            if (newValue != null) {
                model.changeVisualization(newValue.userData.toString())
            } else {
                when (oldValue.userData) {
                    "line graph" -> lineButton.isSelected = true
                    "bar chart" -> barButton.isSelected = true
                    "SEM bar chart" -> barSEMButton.isSelected = true
                    "pie chart" -> pieButton.isSelected = true
                    "box plot" -> boxButton.isSelected = true
                    else -> println("wrong visualization")
                }
            }
        }

        // Add listener to colorGroup
        colorGroup.selectedToggleProperty().addListener { _, _, newValue ->
            model.changeColor(newValue.userData.toString())
        }
    }

    // Set buttons for the bar chart and the pie chart disabled as appropriated
    private fun changeDisable(isNegativeBefore: Boolean) {
        val isNegativeAfter = model.isNegative()
        if (!isNegativeBefore && isNegativeAfter) {
            lineButton.isSelected = true
        }
        if (isNegativeAfter) {
            barSEMButton.setDisable(true)
            pieButton.setDisable(true)
        } else {
            barSEMButton.setDisable(false)
            pieButton.setDisable(false)
        }
    }

    // Handle when a new entry is added to the current data set
    fun handleAdd() {
        model.addData()
    }

    // Handle when an entry is deleted in the current data set
    fun handleDelete(index: Int) {
        val isNegativeBefore = model.isNegative()
        model.deleteData(index)
        changeDisable(isNegativeBefore)
    }

    // Handle when an entry is edited in the current data set
    fun handleEdit(index: Int, newValue: String) {
        val value = newValue.toDoubleOrNull()
        val isNegativeBefore = model.isNegative()
        if (value != null) {
            model.editData(index, value)
        }
        changeDisable(isNegativeBefore)
    }
}
