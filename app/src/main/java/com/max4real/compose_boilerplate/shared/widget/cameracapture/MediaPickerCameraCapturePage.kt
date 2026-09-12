package com.max4real.compose_boilerplate.shared.widget.cameracapture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.shared.util.CustomHaptic
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItemType
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
internal fun MediaPickerCameraCapturePage(
    onDismiss: () -> Unit,
    onImageCaptured: (MediaPickerItem) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        CameraCaptureContent(
            onDismiss = onDismiss,
            onImageCaptured = onImageCaptured
        )
    }
}

@Composable
private fun CameraCaptureContent(
    onDismiss: () -> Unit,
    onImageCaptured: (MediaPickerItem) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val cameraProviderFuture = remember(context) {
        ProcessCameraProvider.getInstance(context)
    }
    var hasCameraPermission by remember(context) {
        mutableStateOf(context.hasCameraCapturePermission())
    }
    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    }
    var camera by remember {
        mutableStateOf<Camera?>(null)
    }
    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }
    var isTorchOn by remember {
        mutableStateOf(false)
    }
    var isCapturing by remember {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, lensFacing, previewView) {
        if (!hasCameraPermission) return@LaunchedEffect

        val cameraProvider = cameraProviderFuture.await(context)
        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        if (!cameraProvider.hasCamera(selector)) return@LaunchedEffect

        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview,
            capture
        )
        imageCapture = capture
        isTorchOn = false
    }

    DisposableEffect(cameraProviderFuture) {
        onDispose {
            cameraProviderFuture.addListener(
                {
                    runCatching {
                        cameraProviderFuture.get().unbindAll()
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(enabled = false) {}
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.matchParentSize()
            )
        } else {
            CameraPermissionContent(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }

        CameraRoundIconButton(
            icon = painterResource(R.drawable.x),
            iconSize = 32,
            contentDescription = "Close camera",
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 18.dp, start = 18.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 34.dp),
            contentAlignment = Alignment.Center
        ) {
            CameraRoundIconButton(
                icon = if (isTorchOn) painterResource(R.drawable.flash) else painterResource(R.drawable.flash_slash),
                contentDescription = "Flash",
                enabled = camera?.cameraInfo?.hasFlashUnit() == true,
                onClick = {
                    CustomHaptic.doubleLightImpact(context)
                    val boundCamera = camera ?: return@CameraRoundIconButton
                    if (!boundCamera.cameraInfo.hasFlashUnit()) return@CameraRoundIconButton

                    val shouldEnable = !isTorchOn
                    boundCamera.cameraControl.enableTorch(shouldEnable)
                    isTorchOn = shouldEnable
                },
                modifier = Modifier
                    .offset(x = (-120).dp)
            )

            CameraCaptureButton(
                isCapturing = isCapturing,
                onClick = {
                    CustomHaptic.doubleLightImpact(context)
                    val capture = imageCapture ?: return@CameraCaptureButton
                    if (isCapturing) return@CameraCaptureButton

                    isCapturing = true
                    val outputFile = context.createMediaPickerCaptureFile()
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                isCapturing = false
                                onImageCaptured(outputFile.toMediaPickerItem())
                            }

                            override fun onError(exception: ImageCaptureException) {
                                isCapturing = false
                            }
                        }
                    )
                }
            )

            CameraRoundIconButton(
                icon = painterResource(R.drawable.refresh_arrow2),
                contentDescription = "Switch camera",
                onClick = {
                    CustomHaptic.doubleLightImpact(context)
                    isTorchOn = false
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier.offset(x = 120.dp)
            )
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera access is needed to take a photo.",
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.W600,
            fontFamily = OpenAISans,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onRequestPermission)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Allow Camera",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
                fontFamily = OpenAISans
            )
        }
    }
}
@Composable
private fun CameraCaptureButton(
    isCapturing: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(CircleShape)
            .border(
                width = 5.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable(
                enabled = !isCapturing,
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isCapturing) 46.dp else 58.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    color = Color.Black,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun CameraRoundIconButton(
    icon: Painter,
    iconSize: Int = 25,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = if (enabled) 0.46f else 0.20f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = if (enabled) 1f else 0.38f),
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

private fun Context.hasCameraCapturePermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.createMediaPickerCaptureFile(): File {
    val directory = File(cacheDir, "media_picker_captures").apply {
        mkdirs()
    }
    return File(directory, "media_capture_${System.currentTimeMillis()}.jpg")
}

private fun File.toMediaPickerItem(): MediaPickerItem {
    return MediaPickerItem(
        uri = Uri.fromFile(this),
        type = MediaPickerItemType.PHOTO,
        displayName = name,
        mimeType = "image/jpeg",
        size = length(),
        dateAddedMillis = lastModified()
    )
}

private suspend fun <T> ListenableFuture<T>.await(context: Context): T {
    return suspendCancellableCoroutine { continuation ->
        addListener(
            {
                try {
                    continuation.resume(get())
                } catch (throwable: Throwable) {
                    continuation.resumeWithException(throwable)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }
}
