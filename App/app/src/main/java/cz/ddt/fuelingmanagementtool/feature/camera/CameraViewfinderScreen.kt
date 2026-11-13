package cz.ddt.fuelingmanagementtool.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cz.ddt.fuelingmanagementtool.ui.icons.AppIcons
import cz.ddt.fuelingmanagementtool.ui.icons.arrow_back
import cz.ddt.fuelingmanagementtool.ui.icons.close
import cz.ddt.fuelingmanagementtool.ui.theme.FuelDarkPrimary
import java.util.concurrent.Executors

@Composable
fun CameraViewfinderScreen(
    onTextRecognized: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraViewfinderContent(
            onTextRecognized = onTextRecognized,
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    } else {
        PermissionRequiredPlaceholder(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

@Composable
private fun CameraViewfinderContent(
    onTextRecognized: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val textRecognizer = remember { OcrTextRecognizer() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    var reticleRect by remember { mutableStateOf(Rect.Zero) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            textRecognizer.close()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF16151D))
            .onSizeChanged { size ->
                previewSize = size
                if (size.width > 0 && size.height > 0) {
                    // Wide (landscape) rectangle matching the Figma mockup
                    val horizontalMargin = size.width * 0.10f
                    val rectWidth = size.width - (horizontalMargin * 2f)
                    val rectHeight = rectWidth * 0.60f
                    val left = horizontalMargin
                    val top = (size.height - rectHeight) * 0.36f
                    reticleRect = Rect(
                        offset = Offset(left, top),
                        size = Size(rectWidth, rectHeight)
                    )
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        errorMessage = "Chyba inicializace kamery: ${e.localizedMessage}"
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!reticleRect.isEmpty) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            ) {
                drawRect(color = Color(0xFF16151D).copy(alpha = 0.78f))

                val cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())

                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = reticleRect.topLeft,
                    size = reticleRect.size,
                    cornerRadius = cornerRadius,
                    blendMode = BlendMode.Clear
                )

                drawRoundRect(
                    color = FuelDarkPrimary,
                    topLeft = reticleRect.topLeft,
                    size = reticleRect.size,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = "Zpět",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Nové tankování",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }

        if (!reticleRect.isEmpty) {
            val labelBottomOffsetDp = with(density) {
                (previewSize.height - reticleRect.top).toDp() + 18.dp
            }
            Text(
                text = "Zarovnej displej stojanu do rámečku",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = labelBottomOffsetDp, start = 24.dp, end = 24.dp)
            )
        }

        errorMessage?.let { msg ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                    if (hasFlash) {
                        isTorchOn = !isTorchOn
                        camera?.cameraControl?.enableTorch(isTorchOn)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                FlashBoltIcon(
                    tint = if (isTorchOn) FuelDarkPrimary else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                    .clickable(enabled = !isProcessing) {
                        isProcessing = true
                        errorMessage = null

                        imageCapture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    try {
                                        val croppedBitmap = ImageCropper().cropToRect(
                                            imageProxy = imageProxy,
                                            rect = reticleRect,
                                            previewViewSize = previewSize
                                        )
                                        imageProxy.close()

                                        textRecognizer.recognizeFromBitmap(
                                            bitmap = croppedBitmap,
                                            onSuccess = { recognizedText ->
                                                isProcessing = false
                                                onTextRecognized(recognizedText)
                                            },
                                            onFailure = { exception ->
                                                isProcessing = false
                                                errorMessage =
                                                    "Rozpoznávání selhalo: ${exception.localizedMessage}"
                                            }
                                        )
                                    } catch (e: Exception) {
                                        imageProxy.close()
                                        isProcessing = false
                                        errorMessage =
                                            "Chyba zpracování snímku: ${e.localizedMessage}"
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    isProcessing = false
                                    errorMessage =
                                        "Pořízení snímku selhalo: ${exception.localizedMessage}"
                                }
                            }
                        )
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .background(color = Color(0xFFD8D6E0), shape = CircleShape)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color(0xFF16151D),
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = "Zavřít",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FlashBoltIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.56f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.54f)
            lineTo(w * 0.50f, h * 0.54f)
            lineTo(w * 0.42f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.50f, h * 0.44f)
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = 1.8.dp.toPx(),
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun PermissionRequiredPlaceholder(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pro snímání displeje stojanu je vyžadován přístup ke kameře.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Povolit kameru")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateBack) {
            Text("Zpět")
        }
    }
}