package com.max4real.compose_boilerplate.shared.widget.mediapicker.gallery_widget

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItemType
import com.max4real.compose_boilerplate.shared.widget.mediapicker.formatPickerDuration
import com.max4real.compose_boilerplate.shared.widget.mediapicker.hasCameraPermission
import com.max4real.compose_boilerplate.shared.widget.mediapicker.loadVideoThumbnail
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun MediaPickerContent(
    mediaItems: List<MediaPickerItem>,
    selectedItems: List<MediaPickerItem>,
    maxSelection: Int,
    onItemClick: (MediaPickerItem) -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCameraPreviewTile: Boolean = true,
    isSinglePickerMode: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    listState: LazyListState = rememberLazyListState(),
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val spacing = 1.dp
        val cellSize = (maxWidth - (spacing * 2)) / 3

        if (showCameraPreviewTile) {
            val firstMediaItems = mediaItems.take(4)
            val remainingRows = mediaItems.drop(4).chunked(3)

            LoadMoreOnScrollEnd(
                listState = listState,
                rowCount = remainingRows.size,
                onLoadMore = onLoadMore
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                contentPadding = contentPadding
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((cellSize * 2) + spacing)
                    ) {
                        CameraPreviewTile(
                            onCameraClick = onCameraClick,
                            modifier = Modifier
                                .width(cellSize)
                                .fillMaxHeight()
                        )

                        Spacer(modifier = Modifier.width(spacing))

                        Column(
                            modifier = Modifier
                                .width((cellSize * 2) + spacing)
                                .fillMaxHeight()
                        ) {
                            MediaPickerGridRow(
                                rowItems = firstMediaItems.take(2),
                                selectedItems = selectedItems,
                                cellSize = cellSize,
                                columnCount = 2,
                                spacing = spacing,
                                maxSelection = maxSelection,
                                onItemClick = onItemClick,
                                isSinglePickerMode = isSinglePickerMode
                            )

                            Spacer(modifier = Modifier.height(spacing))

                            MediaPickerGridRow(
                                rowItems = firstMediaItems.drop(2).take(2),
                                selectedItems = selectedItems,
                                cellSize = cellSize,
                                columnCount = 2,
                                spacing = spacing,
                                maxSelection = maxSelection,
                                onItemClick = onItemClick,
                                isSinglePickerMode = isSinglePickerMode
                            )
                        }
                    }
                }

                if (remainingRows.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(spacing))
                    }
                }

                items(remainingRows) { rowItems ->
                    MediaPickerGridRow(
                        rowItems = rowItems,
                        selectedItems = selectedItems,
                        cellSize = cellSize,
                        columnCount = 3,
                        spacing = spacing,
                        maxSelection = maxSelection,
                        onItemClick = onItemClick,
                        isSinglePickerMode = isSinglePickerMode
                    )
                    Spacer(modifier = Modifier.height(spacing))
                }

                if (isLoadingMore) {
                    item {
                        LoadingMoreFooter()
                    }
                }
            }
        } else {
            MediaPickerPlainGridContent(
                listState = listState,
                mediaItems = mediaItems,
                selectedItems = selectedItems,
                maxSelection = maxSelection,
                onItemClick = onItemClick,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                isSinglePickerMode = isSinglePickerMode,
                isLoadingMore = isLoadingMore,
                onLoadMore = onLoadMore
            )
        }
    }
}

@Composable
internal fun MediaPickerPlainGridContent(
    mediaItems: List<MediaPickerItem>,
    selectedItems: List<MediaPickerItem>,
    maxSelection: Int,
    onItemClick: (MediaPickerItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    isSinglePickerMode: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val spacing = 1.dp
        val cellSize = (maxWidth - (spacing * 2)) / 3
        val rows = mediaItems.chunked(3)

        LoadMoreOnScrollEnd(
            listState = listState,
            rowCount = rows.size,
            onLoadMore = onLoadMore
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            contentPadding = contentPadding
        ) {
            items(rows) { rowItems ->
                MediaPickerGridRow(
                    rowItems = rowItems,
                    selectedItems = selectedItems,
                    cellSize = cellSize,
                    columnCount = 3,
                    spacing = spacing,
                    maxSelection = maxSelection,
                    onItemClick = onItemClick,
                    isSinglePickerMode = isSinglePickerMode
                )
                Spacer(modifier = Modifier.height(spacing))
            }

            if (isLoadingMore) {
                item {
                    LoadingMoreFooter()
                }
            }
        }
    }
}

/**
 * Triggers [onLoadMore] once the user scrolls within a few rows of the end of the list.
 * [rowCount] is the number of already-loaded grid rows (not raw media item count).
 */
@Composable
private fun LoadMoreOnScrollEnd(
    listState: LazyListState,
    rowCount: Int,
    onLoadMore: () -> Unit
) {
    LaunchedEffect(listState, rowCount) {
        if (rowCount <= 0) return@LaunchedEffect

        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.collect { lastVisibleItemIndex ->
            val remainingRows = (listState.layoutInfo.totalItemsCount - 1) - lastVisibleItemIndex
            if (remainingRows <= 6) {
                onLoadMore()
            }
        }
    }
}

@Composable
private fun LoadingMoreFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = theme.mainText,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MediaPickerGridRow(
    rowItems: List<MediaPickerItem>,
    selectedItems: List<MediaPickerItem>,
    cellSize: Dp,
    columnCount: Int,
    spacing: Dp,
    maxSelection: Int,
    onItemClick: (MediaPickerItem) -> Unit,
    isSinglePickerMode: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(cellSize)
    ) {
        repeat(columnCount) { index ->
            val item = rowItems.getOrNull(index)

            if (item == null) {
                Box(
                    modifier = Modifier
                        .width(cellSize)
                        .fillMaxHeight()
                        .background(theme.background)
                )
            } else {
                MediaPickerTile(
                    item = item,
                    selectedIndex = selectedItems.indexOfFirst { it.uri == item.uri } + 1,
                    selectionDisabled = !isSinglePickerMode &&
                            selectedItems.size >= maxSelection &&
                            selectedItems.none { it.uri == item.uri },
                    showSelectionIndicator = !isSinglePickerMode,
                    onClick = {
                        onItemClick(item)
                    },
                    modifier = Modifier
                        .width(cellSize)
                        .fillMaxHeight()
                )
            }

            if (index < columnCount - 1) {
                Spacer(modifier = Modifier.width(spacing))
            }
        }
    }
}

@Composable
private fun MediaPickerTile(
    item: MediaPickerItem,
    selectedIndex: Int,
    selectionDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSelectionIndicator: Boolean = true
) {
    val isSelected = selectedIndex > 0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0xFFE7E7E7))
            .clickable(
                enabled = !selectionDisabled || isSelected,
                onClick = onClick
            )
    ) {
        when (item.type) {
            MediaPickerItemType.PHOTO -> {
                SubcomposeAsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            MediaTilePlaceholder()
                        }

                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }

            MediaPickerItemType.VIDEO -> {
                VideoThumbnail(
                    uri = item.uri,
                    modifier = Modifier.matchParentSize()
                )
            }

            MediaPickerItemType.FILE -> Unit
        }

        if (selectionDisabled && !isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        if (item.type == MediaPickerItemType.VIDEO) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.48f))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )

                item.durationMillis?.let { duration ->
                    2.WidthBox()
                    Text(
                        text = duration.formatPickerDuration(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = OpenAISans,
                        maxLines = 1,
                        letterSpacing = (-0.4f).sp
                    )
                }
            }
        }

        if (showSelectionIndicator) {
            SelectionIndicator(
                selectedIndex = selectedIndex,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
private fun MediaTilePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E2E2)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SelectionIndicator(
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedIndex > 0

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    Color(0xFF2674F4)
                } else {
                    Color.Black.copy(alpha = 0.08f)
                }
            )
            .border(
                width = 1.5.dp,
                color = Color.White,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Text(
                text = selectedIndex.toString(),
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.W800,
                fontFamily = OpenAISans,
                letterSpacing = (-0.4f).sp
            )
        }
    }
}

@Composable
private fun CameraPreviewTile(
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInPreview = LocalInspectionMode.current

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val previewUseCase = remember {
        Preview.Builder().build()
    }
    val hasPermission = context.hasCameraPermission()
    var canShowCameraPreview by remember {
        mutableStateOf(true)
    }
    val cameraProviderFuture = remember(context, isInPreview) {
        if (isInPreview) {
            null
        } else {
            ProcessCameraProvider.getInstance(context)
        }
    }

    DisposableEffect(hasPermission, isInPreview, lifecycleOwner, previewView, previewUseCase) {
        var isDisposed = false
        val mainExecutor = ContextCompat.getMainExecutor(context)

        if (hasPermission && !isInPreview && cameraProviderFuture != null) {
            cameraProviderFuture.addListener(
                {
                    if (isDisposed) return@addListener

                    val cameraProvider = runCatching {
                        cameraProviderFuture.get()
                    }.getOrNull() ?: return@addListener

                    runCatching {
                        previewUseCase.surfaceProvider = previewView.surfaceProvider
                        cameraProvider.unbind(previewUseCase)
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            previewUseCase
                        )
                    }.onSuccess {
                        canShowCameraPreview = true
                    }.onFailure {
                        canShowCameraPreview = false
                    }
                },
                mainExecutor
            )
        }

        onDispose {
            isDisposed = true
            cameraProviderFuture?.addListener(
                {
                    runCatching {
                        cameraProviderFuture.get().unbind(previewUseCase)
                    }
                },
                mainExecutor
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .background(Color.Black)
            .clickable {
                onCameraClick()
            }
    ) {
        if (hasPermission && canShowCameraPreview && !isInPreview) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 9.dp, end = 9.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.40f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.camera),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        } else {
            Icon(
                painter = painterResource(R.drawable.camera_slash),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun VideoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            context.loadVideoThumbnail(uri)
        }
    }

    if (bitmap == null) {
        Box(
            modifier = modifier.background(Color(0xFF202020)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    } else {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
