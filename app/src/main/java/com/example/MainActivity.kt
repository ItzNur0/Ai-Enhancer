package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.EnhancementEntry
import com.example.data.EnhancementRepository
import com.example.ui.EnhancerViewModel
import com.example.ui.EnhancerViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.SampleImages
import com.example.utils.SampleImageItem
import java.io.ByteArrayOutputStream

// Custom Shape for Comparison Slider Clipping (Left side slice)
class SplitShape(private val ratio: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * ratio,
                bottom = size.height
            )
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Persistent Room Setup
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "lumina_enhancer_db"
        ).build()
        val repository = EnhancementRepository(database.enhancementDao())
        val viewModelFactory = EnhancerViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: EnhancerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = viewModelFactory
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: EnhancerViewModel) {
    val context = LocalContext.current
    val original by viewModel.originalBitmap.collectAsStateWithLifecycle()
    val enhanced by viewModel.enhancedBitmap.collectAsStateWithLifecycle()
    val sliderPosition by viewModel.sliderPosition.collectAsStateWithLifecycle()
    val isSimulated by viewModel.isSimulated.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val currentStep by viewModel.processingStep.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMessage.collectAsStateWithLifecycle()
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val intensity by viewModel.toolIntensity.collectAsStateWithLifecycle()
    val tone by viewModel.colorTone.collectAsStateWithLifecycle()

    // Tab Navigation State
    var selectedTab by remember { mutableStateOf(0) } // 0 = Enhanced Lab, 1 = Prompt Generation, 2 = Saved Reels

    // Error Toast
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LUMINA",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                        Text(
                            text = "NEURAL PROCESSING UNIT v3.1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // Premium simulated toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isSimulated) Color(0xFF16102D) else Color(0xFF0052FF).copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (isSimulated) Color(0xFF261D4C) else Color(0xFF00F0FF).copy(alpha = 0.5f),
                                RoundedCornerShape(32.dp)
                            )
                            .clickable { viewModel.toggleMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSimulated) Icons.Default.OfflineBolt else Icons.Default.CloudQueue,
                            contentDescription = "Compute Engine",
                            tint = if (isSimulated) Color(0xFFFF9100) else Color(0xFF00F0FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSimulated) "SIMULATOR" else "CLOUD AI",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF05050C),
                tonalElevation = 8.dp,
                modifier = Modifier.border(0.5.dp, Color(0xFF16102D), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Enhancer Workspace") },
                    label = { Text("Enhance Lab", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F0FF),
                        selectedTextColor = Color(0xFF00F0FF),
                        unselectedIconColor = Color(0xFF6F659C),
                        unselectedTextColor = Color(0xFF6F659C),
                        indicatorColor = Color(0xFF100C1F)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Text Prompt Studio") },
                    label = { Text("Prompt AI", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F0FF),
                        selectedTextColor = Color(0xFF00F0FF),
                        unselectedIconColor = Color(0xFF6F659C),
                        unselectedTextColor = Color(0xFF6F659C),
                        indicatorColor = Color(0xFF100C1F)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Studio History") },
                    label = { Text("Reels", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00F0FF),
                        selectedTextColor = Color(0xFF00F0FF),
                        unselectedIconColor = Color(0xFF6F659C),
                        unselectedTextColor = Color(0xFF6F659C),
                        indicatorColor = Color(0xFF100C1F)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> EnhancerLabTab(viewModel)
                1 -> PromptGenerationTab(viewModel)
                2 -> StudioHistoryTab(viewModel)
            }

            // Universal Processing HUD Overlay
            if (isProcessing) {
                ProcessingOverlayHUD(stepMessage = currentStep)
            }
        }
    }
}

@Composable
fun EnhancerLabTab(viewModel: EnhancerViewModel) {
    val original by viewModel.originalBitmap.collectAsStateWithLifecycle()
    val enhanced by viewModel.enhancedBitmap.collectAsStateWithLifecycle()
    val sliderPosition by viewModel.sliderPosition.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val intensity by viewModel.toolIntensity.collectAsStateWithLifecycle()
    val colorTone by viewModel.colorTone.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. BEFORE/AFTER IMAGE COMPARISON CANVAS
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF261D4C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(top = 8.dp)
                    .testTag("before_after_slider_card")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (original != null && enhanced != null) {
                        BeforeAfterSwipeCompare(
                            before = original!!,
                            after = enhanced!!,
                            ratio = sliderPosition,
                            onRatioChange = { viewModel.setSliderPosition(it) }
                        )
                    } else {
                        // Empty Canvas State
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF080614)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "No items",
                                    tint = Color(0xFF261D4C),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready to Reconstruction",
                                    color = Color(0xFF6F659C),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. CHOOSE SOURCE SAMPLE INPUTS (HORIZONTAL SLIDE)
        item {
            Column {
                Text(
                    text = "SELECT TARGET SOURCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SampleImages.list) { item ->
                        val isSelected = selectedItem.id == item.id
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF16102D) else Color(0xFF090614))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E163B),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectItem(item) }
                                .padding(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item.id) {
                                            "cyber_face" -> Icons.Default.Face
                                            "neon_city" -> Icons.Default.CloudQueue
                                            "macro_core" -> Icons.Default.Search
                                            else -> Icons.Default.Brush
                                        },
                                        contentDescription = item.name,
                                        tint = if (isSelected) Color(0xFF00F0FF) else Color(0xFF6F659C),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.name,
                                    color = if (isSelected) Color.White else Color(0xFFA197C4),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.category,
                                    color = Color(0xFF6F659C),
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. SELECTION OF PREMIUM AI ENHANCEMENT TOOLS
        item {
            Column {
                Text(
                    text = "NEURAL TOOL CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val tools = listOf("HD Upscale", "Noise Reduction", "Face Restoration", "Background Removal", "Color Correction")
                val toolIcons = listOf(
                    Icons.Default.Upload,
                    Icons.Default.GraphicEq,
                    Icons.Default.Face,
                    Icons.Default.Star,
                    Icons.Default.Brush
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tools.size) { index ->
                        val tool = tools[index]
                        val isSelected = activeTool == tool
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) Color(0xFF720DF0) else Color(0xFF100C1F))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFEC00FF) else Color(0xFF261D4C),
                                    RoundedCornerShape(32.dp)
                                )
                                .clickable { viewModel.setActiveTool(tool) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = toolIcons[index],
                                    contentDescription = tool,
                                    tint = if (isSelected) Color.White else Color(0xFFA197C4),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tool,
                                    color = if (isSelected) Color.White else Color(0xFFA197C4),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. FLOATING ENHANCEMENT CONTROLS
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF1E163B)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080614)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENHANCEMENT INTENSITY",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(intensity * 100).toInt()}%",
                            color = Color(0xFF00F0FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = intensity,
                        onValueChange = { viewModel.setIntensity(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00F0FF),
                            activeTrackColor = Color(0xFF720DF0),
                            inactiveTrackColor = Color(0xFF261D4C)
                        ),
                        modifier = Modifier.testTag("intensity_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "LUT COLOR TONE PRESET",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val tones = listOf("Natural", "Cinematic", "Cyber", "Vivid", "Muted")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tones.forEach { t ->
                            val isSelected = colorTone == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF0052FF).copy(alpha = 0.3f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E163B),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setColorTone(t) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t,
                                    color = if (isSelected) Color(0xFF00F0FF) else Color(0xFFA197C4),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. ENHANCE MASTER TRIGGER BUTTON
        item {
            Button(
                onClick = { viewModel.enhanceActiveImage() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF00F0FF), spotColor = Color(0xFFEC00FF))
                    .testTag("enhance_action_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                // Outer background brush mapping
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0052FF), Color(0xFF720DF0), Color(0xFFEC00FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Enhance Action",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENHANCE PHOTO (8K RESOLUTION)",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromptGenerationTab(viewModel: EnhancerViewModel) {
    val context = LocalContext.current
    val testPrompt by viewModel.customPrompt.collectAsStateWithLifecycle()
    val testSize by viewModel.generationSize.collectAsStateWithLifecycle()
    val testModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val original by viewModel.originalBitmap.collectAsStateWithLifecycle()
    val enhanced by viewModel.enhancedBitmap.collectAsStateWithLifecycle()
    val sliderPosition by viewModel.sliderPosition.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // DISPLAY OF LAST ACTION
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF261D4C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (original != null && enhanced != null) {
                        BeforeAfterSwipeCompare(
                            before = original!!,
                            after = enhanced!!,
                            ratio = sliderPosition,
                            onRatioChange = { viewModel.setSliderPosition(it) }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF080614)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ready to generate",
                                    tint = Color(0xFF261D4C),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready to Generate Details",
                                    color = Color(0xFF6F659C),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // TOOL CONTROL SUMMARY BADGES
        item {
            Column {
                Text(
                    text = "SELECT RECONSTRUCTION MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val models = listOf(
                        "gemini-3-pro-image-preview" to "Creator (3 Pro)",
                        "gemini-3.1-flash-image-preview" to "Editor (3.1 Flash)"
                    )
                    models.forEach { (id, label) ->
                        val isSelected = testModel == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF720DF0) else Color(0xFF100C1F))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFEC00FF) else Color(0xFF261D4C),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setSelectedModel(id) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFFA197C4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // INPUT PROMPT FOR IMAGE-TO-IMAGE OR DIRECT CREATION
        item {
            Column {
                Text(
                    text = "AI DESCRIPTION PROMPT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = testPrompt,
                    onValueChange = { viewModel.setCustomPrompt(it) },
                    placeholder = {
                        Text(
                            text = if (testModel == "gemini-3-pro-image-preview") "Enter visual prompt description (e.g. A gorgeous chrome-rendered space shuttle at galactic hyperdrive)..."
                            else "Enter selective editing prompt (e.g. Recalibrate dark contrast, add high glowing cyber goggles)...",
                            color = Color(0xFF6F659C),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("prompt_input_text"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF100C1F),
                        unfocusedContainerColor = Color(0xFF080614),
                        focusedBorderColor = Color(0xFF00F0FF),
                        unfocusedBorderColor = Color(0xFF261D4C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }

        // SPECIFICATION OF GENERATION SIZE (1K, 2K, 4K)
        item {
            Column {
                Text(
                    text = "AFFORDANCE EXPORT RESOLUTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizes = listOf("1K", "2K", "4K")
                    sizes.forEach { size ->
                        val isSelected = testSize == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF0052FF) else Color(0xFF080614))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00F0FF) else Color(0xFF261D4C),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setGenerationSize(size) }
                                .padding(vertical = 12.dp)
                                .testTag("selector_${size.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = size,
                                    color = if (isSelected) Color.White else Color(0xFFA197C4),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (size) {
                                        "1K" -> "1024 px"
                                        "2K" -> "2048 px"
                                        else -> "4096 px (8K)"
                                    },
                                    color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF6F659C),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // TRIGGER BTN
        item {
            Button(
                onClick = { viewModel.generateImageWithPrompt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(16.dp, RoundedCornerShape(27.dp), ambientColor = Color(0xFF720DF0), spotColor = Color(0xFFEC00FF)),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF720DF0), Color(0xFFD500F9), Color(0xFF00D2FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Trigger Creation",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (testModel == "gemini-3.1-flash-image-preview") "PROMPT EDIT ORIGINAL" else "CREATE HD PHOTO ($testSize)",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioHistoryTab(viewModel: EnhancerViewModel) {
    val history by viewModel.historyList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STUDIO ARCHIVE",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${history.size} TOTAL RECONSTRUCTIONS SAVED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF)
                )
            }

            if (history.isNotEmpty()) {
                Text(
                    text = "CLEAR ALL",
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.clearAllHistory()
                            Toast.makeText(context, "Archive formatted successfully", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Empty History",
                        tint = Color(0xFF1E163B),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "History Archive Empty",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Initialize an enhancement or creation rendering to populate the system database.",
                        color = Color(0xFF6F659C),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(history) { entry ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF261D4C)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF090614)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.loadHistoryEntry(entry)
                                Toast.makeText(context, "Studio variables restored", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Render quick thumb of the history image
                            val thumbBitmap = remember(entry.enhancedImage) {
                                try {
                                    val b = Base64.decode(entry.enhancedImage, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(b, 0, b.size)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (thumbBitmap != null) {
                                Image(
                                    bitmap = thumbBitmap.asImageBitmap(),
                                    contentDescription = "History item thumb",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color(0xFF00F0FF), RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.DarkGray)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.toolType,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = entry.prompt,
                                    color = Color(0xFFA197C4),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = entry.parameters,
                                    color = Color(0xFF00F0FF),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteHistoryEntry(entry) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Permanently clear entry from archive",
                                    tint = Color(0xFFFF5252).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeforeAfterSwipeCompare(
    before: Bitmap,
    after: Bitmap,
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        // Absolute pointer tracking detector
        val customDragModifier = Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val currentWidth = size.width.toFloat()
                if (currentWidth > 0) {
                    val deltaRatio = dragAmount.x / currentWidth
                    val target = (ratio + deltaRatio).coerceIn(0.01f, 0.99f)
                    onRatioChange(target)
                }
            }
        }

        // 1. Lower layer: Original blurry/noisy
        Image(
            bitmap = before.asImageBitmap(),
            contentDescription = "Unenhanced input raw file",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Upper layer: Enhanced sharp/color-dense
        Image(
            bitmap = after.asImageBitmap(),
            contentDescription = "Enhanced luxury-tier rendering",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = SplitShape(ratio)
                }
        )

        // GestureDetector Mask
        Box(modifier = customDragModifier.fillMaxSize())

        // 3. Shimmering Neon Divider Line
        val currentX = maxWidth * ratio
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset(x = currentX - 1.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF00F0FF), Color(0xFF720DF0), Color(0xFFEC00FF))
                    )
                )
        )

        // 4. Glossy center control node / thumb
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .offset(x = currentX - 20.dp)
                .background(Color(0xFF040209).copy(alpha = 0.9f), CircleShape)
                .border(1.5.dp, Color(0xFF00F0FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Swipe details left",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Swipe details right",
                    tint = Color(0xFFEC00FF),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Before Label (Sticky bottom-right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "BEFORE [Noisy]",
                color = Color(0xFFFF5252),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // After Label (Sticky bottom-left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color(0xFF00F0FF).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "AFTER [Pro 8K]",
                color = Color(0xFF00F0FF),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProcessingOverlayHUD(stepMessage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040209).copy(alpha = 0.95f))
            .clickable(enabled = false) {}, // Absorb pointer touches
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Rotating Holographic Laser Frame
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .rotate(angle)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color(0xFF00F0FF), Color(0xFF720DF0), Color(0xFFEC00FF), Color(0xFF00F0FF))
                            ),
                            style = Stroke(width = 5.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Neural computation active",
                    tint = Color.White,
                    modifier = Modifier
                        .size(45.dp)
                        .rotate(-angle) // Keep straight
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "NEURAL STITCHING IN PROGRESS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scrolling active terminal command
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1E163B)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF100C1F).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF00F0FF), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stepMessage,
                        color = Color(0xFFA197C4),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
