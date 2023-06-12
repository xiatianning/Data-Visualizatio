import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import kotlin.random.Random

class Model {

    // all views of this model
    private val views: ArrayList<IView> = ArrayList()

    // Register views with the model
    fun addView(view: IView) {
        views.add(view)
        view.updateView()
    }

    // Rainbow color scheme
    private val rainbowList = listOf(
        Color.rgb(255, 0, 0),
        Color.rgb(255, 127, 0),
        Color.rgb(255, 255, 0),
        Color.rgb(0, 255, 0),
        Color.rgb(0, 0, 255),
        Color.rgb(75, 0, 130),
        Color.rgb(148, 0, 211),
    )

    // Gradient gray color scheme
    private val grayList = listOf(
        Color.rgb(210, 210, 210),
        Color.rgb(190, 190, 190),
        Color.rgb(170, 170, 170),
        Color.rgb(150, 150, 150),
        Color.rgb(130, 130, 130),
        Color.rgb(105, 105, 105),
        Color.rgb(90, 90, 90),
    )

    // Gradient gray color scheme
    private val lightList = listOf(
        Color.LIGHTBLUE,
        Color.LIGHTCORAL,
        Color.LIGHTGREEN,
        Color.LIGHTPINK,
        Color.LIGHTSTEELBLUE,
        Color.LIGHTSALMON,
        Color.LIGHTSEAGREEN
    )

    // States of the application
    var currentColor = rainbowList
    var currentDataset = "quadratic"
    var currentVisualization = "line graph"
    val datasetList: ObservableList<String> = FXCollections.observableArrayList(
        "quadratic", "negative quadratic", "alternating", "random", "inflation ‘90-‘22"
    )
    val datasetMap = mutableMapOf(
        "quadratic" to mutableListOf(0.1, 1.0, 4.0, 9.0, 16.0),
        "negative quadratic" to mutableListOf(-0.1, -1.0, -4.0, -9.0, -16.0),
        "alternating" to mutableListOf(-1.0, 3.0, -1.0, 3.0, -1.0, 3.0),
        "random" to MutableList(20) { Random.nextDouble(-100.0, 100.0) },
        "inflation ‘90-‘22" to mutableListOf(
            4.8, 5.6, 1.5, 1.9, 0.2, 2.1, 1.6, 1.6, 1.0, 1.7, 2.7, 2.5, 2.3, 2.8, 1.9, 2.2,
            2.0, 2.1, 2.4, 0.3, 1.8, 2.9, 1.5, 0.9, 1.9, 1.1, 1.4, 1.6, 2.3, 1.9, 0.7, 3.4, 6.8
        )
    )

    // Helper function to determine whether the data set has negative entries
    fun isNegative() : Boolean {
        for (num in datasetMap[currentDataset]!!) {
            if (num < 0.0) {
                return true
            }
        }
        return false
    }

    // Method that the Controller uses to tell the Model that current data set changes
    fun changeDataset(dataset: String) {
        currentDataset = dataset
        views.forEach { it.updateView() }
    }

    // Method that the Controller uses to tell the Model that current visualization changes
    fun changeVisualization(visualization: String) {
        currentVisualization = visualization
        views.forEach { it.updateVisualization() }
    }

    // Method that the Controller uses to tell the Model that current color scheme changes
    fun changeColor(color: String) {
        currentColor = when (color) {
            "gray" -> grayList
            "light" -> lightList
            else -> rainbowList
        }
        views.forEach { it.updateVisualization() }
    }

    // Method that the Controller uses to tell the Model that a new data set is created
    fun addDataset(name: String) {
        currentDataset = name
        datasetList.add(name)
        datasetMap[name] = mutableListOf(0.0)
        views.forEach { it.updateView() }
    }

    // Method that the Controller uses to tell the Model that a new entry is added to the current data set
    fun addData() {
        datasetMap[currentDataset]!!.add(0.0)
        views.forEach { it.updateView() }
    }

    // Method that the Controller uses to tell the Model that an entry is deleted in the current data set
    fun deleteData(index: Int) {
        datasetMap[currentDataset]!!.removeAt(index)
        views.forEach { it.updateView() }
    }

    // Method that the Controller uses to tell the Model that an entry is edited in the current data set
    fun editData(index: Int, value: Double) {
        datasetMap[currentDataset]!![index] = value
        views.forEach { it.updateVisualization() }
    }
}
