import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class Main : Application() {

    override fun start(stage: Stage) {
        // Root Node
        val root = BorderPane()

        // Title
        stage.title = "CS349 - A2 Graphs - t37xia"

        // Create and initialize the model
        val model = Model()

        // Create the Controller, and pass it with the model
        // The controller will handle input and pass requests to the model
        val controller = Controller(model)

        // Create the views, and pass them with the model and controller
        // The views will register themselves and handle displaying the data from the model
        val view = View(model, controller)

        // Set the root
        root.apply {
            top = controller
            center = view
        }

        // Set the scene
        stage.apply {
            minWidth = 640.0;
            minHeight = 480.0;
            scene = Scene(root, 800.0, 600.0)
        }
        stage.show()
    }
}
