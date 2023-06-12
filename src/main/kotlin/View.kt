import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class View(private val model: Model, private val controller: Controller) : SplitPane(), IView {

    // Data entry section on the left of the main section
    private fun dataSection() : ScrollPane {
        val data = model.datasetMap[model.currentDataset]!!
        return ScrollPane(
            VBox(Label("Dataset name: ${model.currentDataset}")).apply {
                spacing = 10.0
                padding = Insets(10.0)
                for (i in data.indices) {
                    children.add(
                        HBox(
                            Label("Entry #$i").apply {
                                padding = Insets(5.0, 0.0, 0.0, 0.0)
                                minWidth = 55.0
                                HBox.setHgrow(this, Priority.NEVER)
                            },
                            TextField().apply {
                                text = data[i].toString()
                                textProperty().addListener { _, _, newValue ->
                                    controller.handleEdit(i, newValue)
                                }
                                maxWidth = Double.MAX_VALUE
                                HBox.setHgrow(this, Priority.ALWAYS)
                            },
                            Button("X").apply {
                                onAction = EventHandler {
                                    controller.handleDelete(i)
                                }
                                if (data.size == 1) {
                                    setDisable(true)
                                }
                                minWidth = 24.0
                                HBox.setHgrow(this, Priority.NEVER)
                            }
                        ).apply {
                            spacing = 10.0
                        }
                    )
                }
                children.add(
                    Button("Add Entry").apply {
                        onAction = EventHandler {
                            controller.handleAdd()
                        }
                        maxWidth = Double.MAX_VALUE
                    }
                )
            }
        ).apply {
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        }
    }

    // Visualization section on the right of the main section
    private fun visualizationSection() : Pane {
        val container = Pane()
        container.background = Background(BackgroundFill(Color.WHITE, CornerRadii(0.0), Insets.EMPTY))
        container.children.add(Group(graphFunc(396.0, 541.0)))
        container.widthProperty().addListener { _, _, _ ->
            container.children.clear()
            container.children.add(Group(graphFunc(container.width, container.height)))
        }
        container.heightProperty().addListener { _, _, _ ->
            container.children.clear()
            container.children.add(Group(graphFunc(container.width, container.height)))
        }
        return container
    }

    // Draw current visualization
    private fun graphFunc(containerWidth : Double, containerHeight : Double) : List<Node> {
        return when (model.currentVisualization) {
            "line graph" -> lineGraph(containerWidth, containerHeight)
            "bar chart" -> barChart(containerWidth, containerHeight)
            "SEM bar chart" -> barChartSEM(containerWidth, containerHeight)
            "pie chart" -> pieChart(containerWidth, containerHeight)
            else -> boxPlot(containerWidth, containerHeight)
        }
    }

    // Generate nodes for Line Graph
    private fun lineGraph(containerWidth : Double, containerHeight : Double) : List<Node> {
        val data = model.datasetMap[model.currentDataset]!!
        val nodeList = mutableListOf<Node>()
        val margin = 12.5
        val width = containerWidth - margin * 2   // Leave the margin to left and right
        val height = containerHeight - margin * 2  // Leave the margin to top and bottom
        if (data.size == 1) {  // If there is only one value in the data set, simply draw one marker
            nodeList.add(
                Rectangle(5.0, 5.0, Color.RED).apply {
                    x = width / 2 + margin - 2.5
                    y = height + margin - 2.5
                }
            )
            nodeList.add(statSummary())
            return nodeList
        }
        val minValue = data.min()
        val maxValue = data.max()
        val range = maxValue - minValue
        val separation = width / (data.size - 1)
        val xIndices = mutableListOf<Double>()
        val yIndices = mutableListOf<Double>()
        for (i in data.indices) {
            xIndices.add(i * separation + margin)
            if (range == 0.0) {
                yIndices.add(height + margin)
            } else {
                yIndices.add(height - (data[i] - minValue) / range * height + margin)
            }
        }
        for (i in data.indices) {
            nodeList.add(
                Rectangle(5.0, 5.0, Color.RED).apply {
                    x = xIndices[i] - 2.5
                    y = yIndices[i] - 2.5
                }
            )
            if (i + 1 < data.size) {
                nodeList.add(
                    Line(xIndices[i], yIndices[i], xIndices[i + 1], yIndices[i + 1])
                )
            }
        }
        nodeList.add(statSummary())
        return nodeList
    }

    // Generate nodes for bar chart
    private fun barChart(containerWidth : Double, containerHeight : Double) : List<Node> {
        val data = model.datasetMap[model.currentDataset]!!
        val nodeList = mutableListOf<Node>()
        val margin = 10.0
        val width = containerWidth - margin * 2   // Leave the margin to left and right
        val height = containerHeight - margin * 2  // Leave the margin to top and bottom
        var minValue = data.min()
        var maxValue = data.max()
        if (minValue > 0.0) minValue = 0.0
        if (maxValue < 0.0) maxValue = 0.0
        val range = maxValue - minValue
        val zeroLineHeight = if (range != 0.0) {
            height - (0.0 - minValue) / range * height + margin
        } else {
            height + margin
        }
        val separation = width / (data.size * 2 - 1)
        for (i in data.indices) {
            if (data[i] == 0.0) {
                continue
            }
            val barHeight = if (range != 0.0) {
                abs(data[i]) / range * height
            } else {
                height
            }
            nodeList.add(
                Rectangle(separation, barHeight, model.currentColor[i % 7]).apply {
                    x = 2 * i * separation + margin
                    y = if (data[i] > 0.0) {
                        zeroLineHeight - barHeight
                    } else {
                       zeroLineHeight
                    }
                }
            )
        }
        nodeList.add(
            Line(margin, zeroLineHeight, width + margin, zeroLineHeight)  // 0.0 line
        )
        nodeList.add(statSummary())
        return nodeList
    }

    // Generate nodes for SEM bar chart
    private fun barChartSEM(containerWidth : Double, containerHeight : Double) : List<Node> {
        val data = model.datasetMap[model.currentDataset]!!
        val nodeList = mutableListOf<Node>()
        val margin = 10.0
        val width = containerWidth - margin * 2   // Leave the margin to left and right
        val height = containerHeight - margin * 2  // Leave the margin to top and bottom
        val maxValue = data.max()
        val range = maxValue - 0.0
        val separation = width / (data.size * 2 - 1)
        var allZeros = true
        for (i in data.indices) {
            if (data[i] == 0.0) {
                continue
            }
            allZeros = false
            val barHeight = if (range != 0.0) {
                abs(data[i]) / range * height
            } else {
                height
            }
            nodeList.add(
                Rectangle(separation, barHeight, model.currentColor[i % 7]).apply {
                    x = 2 * i * separation + margin
                    y = height - barHeight + margin
                }
            )
        }
        nodeList.add(
            Line(margin, height + margin, width + margin, height + margin)  // 0.0 line
        )
        if (allZeros) {
            nodeList.add(statSummary())
            return nodeList
        }
        val mean = data.average()
        val meanHeight = if (range != 0.0) {
            height - mean / range * height + margin
        } else {
            margin
        }
        nodeList.add(
            Line(margin, meanHeight, width + margin, meanHeight)  // line indicating the mean
        )
        val standardError = findStandardError(data)
        val upperHeight = if (range != 0.0) {
            height - (mean + standardError) / range * height + margin
        } else {
            margin
        }
        val upperLine = Line(margin, upperHeight, width + margin, upperHeight)  // line indicating the upper SEM
        upperLine.strokeDashArray.add(5.0)
        nodeList.add(upperLine)
        val lowerHeight = if (range != 0.0) {
            height - (mean - standardError) / range * height + margin
        } else {
            margin
        }
        val lowerLine = Line(margin, lowerHeight, width + margin, lowerHeight)  // line indicating the lower SEM
        lowerLine.strokeDashArray.add(5.0)
        nodeList.add(lowerLine)
        nodeList.add(statSummary())
        return nodeList
    }

    // Generate nodes for pie chart
    private fun pieChart(containerWidth : Double, containerHeight : Double) : List<Node> {
        val data = model.datasetMap[model.currentDataset]!!
        val nodeList = mutableListOf<Node>()
        val margin = 10.0
        val width = containerWidth - margin * 2   // Leave the margin to left and right
        val height = containerHeight - margin * 2  // Leave the margin to top and bottom
        val sum = data.sum()
        val radius = minOf(width, height) / 2
        var angle = 0.0
        for (i in data.indices) {
            if (data[i] == 0.0) {
                continue
            }
            nodeList.add(
                Arc().apply {
                    centerX = width / 2 + margin
                    centerY = height / 2 + margin
                    radiusX = radius
                    radiusY = radius
                    startAngle = angle
                    length = data[i] / sum * 360
                    type = ArcType.ROUND
                    fill = model.currentColor[i % 7]
                }
            )
            angle += data[i] / sum * 360
        }
        nodeList.add(statSummary())
        return nodeList
    }

    // Generate nodes for box plot
    private fun boxPlot(containerWidth : Double, containerHeight : Double) : List<Node> {
        val nodeList = mutableListOf<Node>()
        val margin = 12.5
        val width = containerWidth - margin * 2   // Leave the margin to left and right
        val height = containerHeight - margin * 2  // Leave the margin to top and bottom
        val summary = statSummaryList()
        val min = summary[2]
        val lowerQuartile = summary[3]
        val median = summary[4]
        val upperQuartile = summary[5]
        val max = summary[6]
        val range = max - min
        if (range == 0.0) {
            nodeList.add(
                Rectangle(5.0, 5.0, Color.RED).apply {
                    x = width / 2 + margin - 2.5
                    y = height / 2 + margin - 2.5
                }
            )
            nodeList.add(
                Line(
                    width / 2 + margin,
                    height / 2 - 35.0 + margin,
                    width / 2 + margin,
                    height / 2 + 35.0 + margin)
            )
            nodeList.add(statSummary())
            return nodeList
        }
        val lowerWidth = (lowerQuartile - min) / range * width
        nodeList.add(
            Line(margin, height / 2 + margin, lowerWidth + margin, height / 2 + margin)
        )
        val upperWidth = (upperQuartile - min) / range * width
        nodeList.add(
            Line(upperWidth + margin, height / 2 + margin, width + margin, height / 2 + margin)
        )
        nodeList.add(
            Rectangle(upperWidth - lowerWidth, 70.0, Color.TRANSPARENT).apply {
                x = lowerWidth + margin
                y = height / 2 - 35.0 + margin
                stroke = Color.BLACK
                strokeWidth = 1.0
            }
        )
        val middleWidth = (median - min) / range * width
        nodeList.add(
            Line(
                middleWidth + margin,
                height / 2 - 35.0 + margin,
                middleWidth + margin,
                height / 2 + 35.0 + margin
            )
        )
        nodeList.add(
            Rectangle(5.0, 5.0, Color.RED).apply {
                x = margin - 2.5
                y = height / 2 + margin - 2.5
            }
        )
        nodeList.add(
            Rectangle(5.0, 5.0, Color.RED).apply {
                x = width + margin- 2.5
                y = height / 2 + margin - 2.5
            }
        )
        nodeList.add(statSummary())
        return nodeList
    }

    // Helper function for finding the standard error of the data set
    private fun findStandardError(data: MutableList<Double>) : Double {
        var standardError = 0.0
        for (num in data) {
            standardError += (num - data.average()).pow(2.0)
        }
        standardError = (sqrt(standardError / data.size)) / sqrt(data.size.toDouble())
        return standardError
    }

    // Helper function for finding the median of the part of data set from startIndex to endIndex
    private fun findMedian(data: MutableList<Double>, startIndex: Int, endIndex: Int) : Double {
        val size = endIndex - startIndex + 1
        val middleIndex = startIndex + size / 2
        return if (size % 2 == 1) {
            data[middleIndex]
        } else {
            (data[middleIndex - 1] + data[middleIndex]) / 2.0
        }
    }

    // Helper function for calculating mean, standard error, and five number summary of current data set
    private fun statSummaryList() : List<Double> {
        val data = model.datasetMap[model.currentDataset]!!
        val sortedData = mutableListOf<Double>()
        for (num in data) sortedData.add(num)
        sortedData.sort()
        val mean = sortedData.average()
        val standardError = findStandardError(sortedData)
        val min = sortedData[0]
        val max = sortedData[sortedData.size - 1]
        val median = findMedian(sortedData, 0, sortedData.size - 1)
        val upperStart = sortedData.size / 2
        val lowerEnd = if (sortedData.size % 2 == 0) {
            upperStart - 1
        } else {
            upperStart
        }
        val lowerQuartile = findMedian(sortedData, 0, lowerEnd)
        val upperQuartile = findMedian(sortedData, upperStart, sortedData.size - 1)
        return listOf(mean, standardError, min, lowerQuartile, median, upperQuartile, max)
    }

    // Generate the statistical information of the current data set
    private fun statSummary() : Node {
        val summary = statSummaryList()
        return VBox(
            Label("mean: ${String.format("%.2f", summary[0])}"),
            Label("SEM: ${String.format("%.2f", summary[1])}"),
            Label("minimum value: ${String.format("%.2f", summary[2])}"),
            Label("lower quartile: ${String.format("%.2f", summary[3])}"),
            Label("median: ${String.format("%.2f", summary[4])}"),
            Label("upper quartile: ${String.format("%.2f", summary[5])}"),
            Label("maximum value: ${String.format("%.2f", summary[6])}"),
        ).apply{
            padding = Insets(10.0)
            background = Background(
                BackgroundFill(
                    Color.rgb(255, 255, 255, 0.7),
                    CornerRadii(0.0),
                    Insets.EMPTY
                )
            )
        }
    }

    // Update the visualization display when being notified by the model
    override fun updateVisualization() {
        items[1] = visualizationSection()
    }

    // Update the whole display when being notified by the model
    override fun updateView() {
        items.clear()
        items.add(dataSection())
        items.add(visualizationSection())
    }

    init {
        // Register with the model
        model.addView(this)
    }
}
