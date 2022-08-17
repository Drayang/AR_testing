package com.example.artesting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.artesting.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.sceneform.lullmodel.VertexAttributeUsage.Position
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var placeModelButton: ExtendedFloatingActionButton
    lateinit var newModelButton: ExtendedFloatingActionButton

    ////////////////////////////////////////////////////////////////////////////////////
    data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE
    )

    val models = listOf(
        Model("models/spiderbot.glb"),
        Model(
            "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
            placementMode = PlacementMode.BEST_AVAILABLE.apply {
                keepRotation = true
            },
            // Display the Tiger with a size of 3 m long
            scaleUnits = 2.5f
        ),
        Model(
            "https://sceneview.github.io/assets/models/DamagedHelmet.glb",
            placementMode = PlacementMode.INSTANT,
            scaleUnits = 0.5f
        ),
        Model(
            "https://storage.googleapis.com/ar-answers-in-search-models/static/GiantPanda/model.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL,
            // Display the Tiger with a size of 1.5 m height
            scaleUnits = 0.5f
        ),
        Model(
            "https://sceneview.github.io/assets/models/Spoons.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL,
            // Keep original model size
            scaleUnits = null
        ),
        Model(
            "https://sceneview.github.io/assets/models/Halloween.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL,
            scaleUnits = 2.5f
        ),
    )
    ////////////////////////////////////////////////////////////////////////////////////

    // ARModelNode
    var modelIndex = 0
    var modelNode: ArModelNode? = null

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )


        sceneView = findViewById(R.id.sceneView)
        loadingView = findViewById(R.id.loadingView)

        newModelButton = findViewById<ExtendedFloatingActionButton>(R.id.newModelButton).apply {
            // Add system bar margins
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener { newModelNode() }
        }
        placeModelButton = findViewById<ExtendedFloatingActionButton>(R.id.placeModelButton).apply {
            setOnClickListener { placeModelNode() }
        }

        newModelNode()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.activity_main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        item.isChecked = !item.isChecked
//        modelNode?.detachAnchor()
//        modelNode?.placementMode = when (item.itemId) {
//            R.id.menuPlanePlacement -> PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL
//            R.id.menuInstantPlacement -> PlacementMode.INSTANT
//            R.id.menuDepthPlacement -> PlacementMode.DEPTH
//            R.id.menuBestPlacement -> PlacementMode.BEST_AVAILABLE
//            else -> PlacementMode.DISABLED
//        }
//        return super.onOptionsItemSelected(item)
//    }

    fun placeModelNode() {
        modelNode?.anchor()
        placeModelButton.isVisible = false
        sceneView.planeRenderer.isVisible = false //the many many small dots de
    }

    fun newModelNode() {
        isLoading = true
        modelNode?.takeIf { //to check first time  call or not
            !it.isAnchored }?.let { //if the model node not anchor yet,
            sceneView.removeChild(it)
            it.destroy()
        }
        val model = models[modelIndex]
        modelIndex = (modelIndex + 1) % models.size
        modelNode = ArModelNode(model.placementMode).apply {
            loadModelAsync( //try to load the model... This is function of ArModelNOde
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = model.scaleUnits,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) { // after successfully loading the model then....
                sceneView.planeRenderer.isVisible = true
                isLoading = false
            }
            onPoseChanged = { node, _ ->
                placeModelButton.isGone = node.isAnchored || !node.isTracking
            }
        }
        sceneView.addChild(modelNode!!) //recode the ARmodelNode to our sceneview
        // Select the model node by default (the model node is also selected on tap)
        sceneView.selectedNode = modelNode
    }

}