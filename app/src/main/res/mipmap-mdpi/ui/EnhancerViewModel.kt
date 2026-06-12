package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.data.EnhancementEntry
import com.example.data.EnhancementRepository
import com.example.utils.SampleImages
import com.example.utils.SampleImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class EnhancerViewModel(private val repository: EnhancementRepository) : ViewModel() {

    // Selected Sample Image
    private val _selectedItem = MutableStateFlow(SampleImages.list[0])
    val selectedItem: StateFlow<SampleImageItem> = _selectedItem.asStateFlow()

    // Bitmaps
    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _enhancedBitmap = MutableStateFlow<Bitmap?>(null)
    val enhancedBitmap: StateFlow<Bitmap?> = _enhancedBitmap.asStateFlow()

    // Mode: Simulation vs Cloud Gemini API
    private val _isSimulated = MutableStateFlow(true)
    val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    // Progress and UI state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingStep = MutableStateFlow("")
    val processingStep: StateFlow<String> = _processingStep.asStateFlow()

    private val _sliderPosition = MutableStateFlow(0.5f)
    val sliderPosition: StateFlow<Float> = _sliderPosition.asStateFlow()

    private val _activeTool = MutableStateFlow("HD Upscale")
    val activeTool: StateFlow<String> = _activeTool.asStateFlow()

    private val _toolIntensity = MutableStateFlow(0.8f)
    val toolIntensity: StateFlow<Float> = _toolIntensity.asStateFlow()

    private val _colorTone = MutableStateFlow("Cinematic")
    val colorTone: StateFlow<String> = _colorTone.asStateFlow()

    // Core Custom Prompt, Model and Affordance controls
    private val _customPrompt = MutableStateFlow("")
    val customPrompt: StateFlow<String> = _customPrompt.asStateFlow()

    private val _generationSize = MutableStateFlow("2K") // 1K, 2K, 4K
    val generationSize: StateFlow<String> = _generationSize.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3-pro-image-preview") // Or gemini-3.1-flash-image-preview
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Room DB History
    val historyList: StateFlow<List<EnhancementEntry>> = repository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load initial bitmaps from selected sample
        loadSampleItem(_selectedItem.value)
    }

    fun selectItem(item: SampleImageItem) {
        _selectedItem.value = item
        loadSampleItem(item)
    }

    private fun loadSampleItem(item: SampleImageItem) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                _originalBitmap.value = SampleImages.generate(item.id, false)
                _enhancedBitmap.value = SampleImages.generate(item.id, true)
                _sliderPosition.value = 0.5f
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun setSliderPosition(position: Float) {
        _sliderPosition.value = position
    }

    fun setIntensity(intensity: Float) {
        _toolIntensity.value = intensity
    }

    fun setColorTone(tone: String) {
        _colorTone.value = tone
    }

    fun setActiveTool(tool: String) {
        _activeTool.value = tool
        // Find matching sample if possible
        val matchingSample = SampleImages.list.find { it.category == tool }
        if (matchingSample != null) {
            selectItem(matchingSample)
        }
    }

    fun setCustomPrompt(prompt: String) {
        _customPrompt.value = prompt
    }

    fun setGenerationSize(size: String) {
        _generationSize.value = size
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun toggleMode() {
        _isSimulated.value = !_isSimulated.value
    }

    // Convert Bitmap to Base64 String
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // Main Enhancement Processing Loop
    fun enhanceActiveImage() {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            _sliderPosition.value = 0.5f

            if (_isSimulated.value) {
                // Highly visual multi-step neural simulation
                val steps = listOf(
                    "Establishing neural mapping matrices...",
                    "Computing edge-luminance vector differentials...",
                    "Executing deep-restoration facial landmark alignment...",
                    "Cleaning chroma artifacts and digital lens noise...",
                    "Applying dynamic ${colorTone.value} color calibration...",
                    "Upscaling details to high-fidelity resolution...",
                    "Writing 8K subpixel density streams..."
                )

                for (step in steps) {
                    _processingStep.value = step
                    delay(800)
                }

                // Generate and assign precomputed high fidelity output
                val orig = _originalBitmap.value ?: SampleImages.generate(_selectedItem.value.id, false)
                val enh = SampleImages.generate(_selectedItem.value.id, true)
                _originalBitmap.value = orig
                _enhancedBitmap.value = enh

                // Save to Room Database
                val entry = EnhancementEntry(
                    originalImage = bitmapToBase64(orig),
                    enhancedImage = bitmapToBase64(enh),
                    toolType = _activeTool.value,
                    prompt = "Smart Simulation - Custom ${colorTone.value} Grade",
                    parameters = "Intensity: ${(toolIntensity.value * 100).toInt()}%"
                )
                repository.saveEntry(entry)
                
                // Sweep visual feedback slider to complete effect
                animateSliderComplete()
            } else {
                // cloud AI execution using Direct REST API with configured Gemini Key
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _errorMessage.value = "Gemini API Key missing. Please provide it in the AI Studio SECRETS panel, or toggle 'Simulation Mode' to enjoy full offline capabilities."
                    _isProcessing.value = false
                    return@launch
                }

                _processingStep.value = "Sending request to Gemini Cloud Engine..."
                val original = _originalBitmap.value
                if (original == null) {
                    _errorMessage.value = "No original image loaded to enhance."
                    _isProcessing.value = false
                    return@launch
                }

                try {
                    // Convert bitmap to Base64 for the inline data part
                    val base64Img = withContext(Dispatchers.IO) { bitmapToBase64(original) }
                    
                    val instruction = "You are a professional luxury-tier AI image enhancer. " +
                            "Apply the enhancement parameter '${_activeTool.value}' with color tone trend '${_colorTone.value}' and detail multiplier intensity '${_toolIntensity.value}'. " +
                            "Output the enhanced, high-definition version of this image as an IMAGE modality. Return ONLY the high definition image."

                    val request = GenerateContentRequest(
                        contents = listOf(
                            Content(
                                parts = listOf(
                                    Part(text = instruction),
                                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img))
                                )
                            )
                        ),
                        generationConfig = GenerationConfig(
                            responseModalities = listOf("IMAGE")
                        )
                    )

                    _processingStep.value = "Running Neural Reconstruction on cloud..."
                    val model = "gemini-3.1-flash-image-preview" // Default enhancer model
                    val response = RetrofitClient.service.generateContent(model, apiKey, request)
                    
                    val inlinePart = response.candidates?.firstOrNull()?.content?.parts?.find { it.inlineData != null }?.inlineData
                    if (inlinePart?.data != null) {
                        val decodedBytes = Base64.decode(inlinePart.data, Base64.DEFAULT)
                        val resultBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (resultBitmap != null) {
                            _enhancedBitmap.value = resultBitmap
                            
                            // Save to Room DB
                            val entry = EnhancementEntry(
                                originalImage = bitmapToBase64(original),
                                enhancedImage = inlinePart.data,
                                toolType = _activeTool.value,
                                prompt = "Cloud AI - Model: $model",
                                parameters = "Intensity: ${(toolIntensity.value * 100).toInt()}%"
                            )
                            repository.saveEntry(entry)
                            animateSliderComplete()
                        } else {
                            _errorMessage.value = "Cloud successfully completed, but could not decode result stream."
                        }
                    } else {
                        _errorMessage.value = "Cloud did not return structured image data. Check standard parameters, or use Simulation."
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Cloud API Failed: ${e.localizedMessage ?: "Connection Timeout"}. Please utilize the absolute offline Smart Simulation."
                }
            }
            _isProcessing.value = false
        }
    }

    // AI Create / Edit Image Generation Flow
    fun generateImageWithPrompt() {
        viewModelScope.launch {
            if (_customPrompt.value.isBlank()) {
                _errorMessage.value = "Please enter an enhancement or creation prompt."
                return@launch
            }

            _isProcessing.value = true
            _errorMessage.value = null
            _sliderPosition.value = 0.5f

            if (_isSimulated.value) {
                // Simulated Creation
                val steps = listOf(
                    "Parsing cinematic vocabulary tokens...",
                    "Sampling structural detail models...",
                    "Synthesizing customized layout shapes...",
                    "Multiplying subpixels dynamically...",
                    "Polishing contrast to ${_generationSize.value} HDR grade..."
                )

                for (step in steps) {
                    _processingStep.value = step
                    delay(800)
                }

                // Create a simulated beautiful custom photo based on customPrompt
                val mockCreated = createSimulatedBitmapFromPrompt(_customPrompt.value)
                _originalBitmap.value = SampleImages.generate("cyber_face", false) // mock "before"
                _enhancedBitmap.value = mockCreated

                val entry = EnhancementEntry(
                    originalImage = bitmapToBase64(_originalBitmap.value!!),
                    enhancedImage = bitmapToBase64(mockCreated),
                    toolType = "AI Image Gen",
                    prompt = _customPrompt.value,
                    parameters = _generationSize.value
                )
                repository.saveEntry(entry)
                animateSliderComplete()
            } else {
                // Real Gemini API Call!
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _errorMessage.value = "Gemini API Key missing. Please provide it in the AI Studio SECRETS panel."
                    _isProcessing.value = false
                    return@launch
                }

                val model = _selectedModel.value // "gemini-3-pro-image-preview" or "gemini-3.1-flash-image-preview"
                _processingStep.value = "Calling Gemini endpoint using $model..."
                
                try {
                    val promptText = if (model == "gemini-3.1-flash-image-preview") {
                        // Image editing flow
                        val original = _originalBitmap.value
                        val base64Img = original?.let { bitmapToBase64(it) }
                        "Apply edit instructions: '${_customPrompt.value}'. Make the styling premium, ultra HD resolution. Return ONLY the edited image."
                    } else {
                        // General creation flow
                        "Create a premium, award-winning, 8K ultra detailed luxury visual: ${_customPrompt.value}"
                    }

                    val original = _originalBitmap.value ?: SampleImages.generate("cyber_face", false)
                    val base64Img = withContext(Dispatchers.IO) { bitmapToBase64(original) }

                    val parts = mutableListOf<Part>()
                    if (model == "gemini-3.1-flash-image-preview") {
                        parts.add(Part(text = promptText))
                        parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img)))
                    } else {
                        parts.add(Part(text = promptText))
                    }

                    val req = GenerateContentRequest(
                        contents = listOf(Content(parts = parts)),
                        generationConfig = GenerationConfig(
                            imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = _generationSize.value),
                            responseModalities = listOf("IMAGE")
                        )
                    )

                    _processingStep.value = "Generating pixel stream on model ($model)..."
                    val response = RetrofitClient.service.generateContent(model, apiKey, req)
                    
                    val inlinePart = response.candidates?.firstOrNull()?.content?.parts?.find { it.inlineData != null }?.inlineData
                    if (inlinePart?.data != null) {
                        val decodedBytes = Base64.decode(inlinePart.data, Base64.DEFAULT)
                        val resultBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (resultBitmap != null) {
                            _originalBitmap.value = original
                            _enhancedBitmap.value = resultBitmap
                            
                            val entry = EnhancementEntry(
                                originalImage = bitmapToBase64(original),
                                enhancedImage = inlinePart.data,
                                toolType = "AI Image Gen",
                                prompt = _customPrompt.value,
                                parameters = _generationSize.value
                            )
                            repository.saveEntry(entry)
                            animateSliderComplete()
                        } else {
                            _errorMessage.value = "Image generated, but stream decoding failed."
                        }
                    } else {
                        _errorMessage.value = "Model did not return binary image. Check key permissions."
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Cloud Gen Failed: ${e.localizedMessage ?: "Unknown service error"}"
                }
            }
            _isProcessing.value = false
        }
    }

    private fun createSimulatedBitmapFromPrompt(prompt: String): Bitmap {
        val width = 600
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Generate a futuristic neon scene with the user's prompt as overlay text!
        canvas.drawColor(Color.parseColor("#060010"))

        val radial = android.graphics.RadialGradient(
            width / 2f, height / 2f, width * 0.5f,
            Color.parseColor("#B300FF"), Color.parseColor("#000B2B"),
            Shader.TileMode.CLAMP
        )
        paint.shader = radial
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Geometric cool rings
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#00D2FF")
        canvas.drawCircle(width / 2f, height / 2f, 180f, paint)

        paint.color = Color.parseColor("#FF007F")
        canvas.drawCircle(width / 2f, height / 2f, 210f, paint)

        // Draw an absolute luxury prism
        val path = Path().apply {
            moveTo(width / 2f, height / 2f - 100f)
            lineTo(width / 2f - 120f, height / 2f + 80f)
            lineTo(width / 2f + 120f, height / 2f + 80f)
            close()
        }
        val prismGrad = android.graphics.LinearGradient(
            width / 2f, height / 2f - 100f, width / 2f, height / 2f + 80f,
            Color.parseColor("#00FFF6"), Color.parseColor("#E100FF"),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.shader = prismGrad
        canvas.drawPath(path, paint)
        paint.shader = null

        // Overlay text based on prompt
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        
        val truncatedPrompt = if (prompt.length > 28) prompt.take(25) + "..." else prompt
        canvas.drawText("AI GENERATION SUCCESS", width / 2f, height / 2f + 140f, paint)
        
        paint.color = Color.parseColor("#00FFFF")
        paint.textSize = 22f
        canvas.drawText("\"$truncatedPrompt\"", width / 2f, height / 2f + 180f, paint)

        return bitmap
    }

    private suspend fun animateSliderComplete() {
        // Start from left (0.0), sweep gracefully to right (1.0), settling at center (0.5) for comparison
        _sliderPosition.value = 0.0f
        delay(150)
        _sliderPosition.value = 0.2f
        delay(150)
        _sliderPosition.value = 0.5f
        delay(150)
        _sliderPosition.value = 0.8f
        delay(150)
        _sliderPosition.value = 0.5f
    }

    fun loadHistoryEntry(entry: EnhancementEntry) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val orig = SampleImages.fromBase64(entry.originalImage)
                val enh = SampleImages.fromBase64(entry.enhancedImage)
                if (orig != null && enh != null) {
                    _originalBitmap.value = orig
                    _enhancedBitmap.value = enh
                    _activeTool.value = entry.toolType
                    _customPrompt.value = entry.prompt
                    _sliderPosition.value = 0.5f
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun deleteHistoryEntry(entry: EnhancementEntry) {
        viewModelScope.launch {
            repository.removeEntry(entry)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

class EnhancerViewModelFactory(private val repository: EnhancementRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnhancerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
