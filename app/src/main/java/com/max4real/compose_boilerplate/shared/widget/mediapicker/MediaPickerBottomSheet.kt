package com.max4real.compose_boilerplate.shared.widget.mediapicker

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.extensions.HeightBox
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.shared.util.CustomHaptic
import com.max4real.compose_boilerplate.shared.widget.AppDialogFrame
import com.max4real.compose_boilerplate.shared.widget.AppDialogMessage
import com.max4real.compose_boilerplate.shared.widget.AppDialogTextActions
import com.max4real.compose_boilerplate.shared.widget.AppDialogTitle
import com.max4real.compose_boilerplate.shared.widget.CustomBottomSheetPlate
import com.max4real.compose_boilerplate.shared.widget.TopGradient
import com.max4real.compose_boilerplate.shared.widget.cameracapture.MediaPickerCameraCapturePage
import com.max4real.compose_boilerplate.shared.widget.mediapicker.file_widgets.FilePickerContent
import com.max4real.compose_boilerplate.shared.widget.mediapicker.gallery_widget.MediaPickerContent
import com.max4real.compose_boilerplate.ui.theme.CustomColor
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MediaPickerTab {
    GALLERY,
    FILE
}


@Composable
fun MediaPickerBottomSheet(
    mode: MediaPickerMode,
    selectedItems: List<MediaPickerItem> = emptyList(),
    onDismiss: () -> Unit,
    onSelect: OnMediaPickerSelect,
    onSelectionCleared: () -> Unit = {},
    modifier: Modifier = Modifier,
    sheetHeightFraction: Float = 0.82f,
    maxSelection: Int = Int.MAX_VALUE,
    includeCameraPreview: Boolean = true,
    isSinglePickerMode: Boolean = false

) {
    val context = LocalContext.current
    val mediaMode =
        if (mode == MediaPickerMode.PHOTO) MediaPickerMode.PHOTO else MediaPickerMode.MEDIA
    var selectedTab by remember(mode) {
        mutableStateOf(MediaPickerTab.GALLERY)
    }
    var permissionState by remember(mode) {
        mutableStateOf(context.mediaPermissionState(mediaMode))
    }
    var mediaItems by remember(mode) {
        mutableStateOf<List<MediaPickerItem>>(emptyList())
    }
    var albumList by remember(mode) {
        mutableStateOf<List<MediaPickerAlbum>>(emptyList())
    }
    var selectedAlbum by remember(mode) {
        mutableStateOf<MediaPickerAlbum?>(null)
    }
    var showAlbumPicker by remember(mode) {
        mutableStateOf(false)
    }
    var isMediaLoading by remember(mode) {
        mutableStateOf(false)
    }
    var isLoadingMoreMedia by remember(mode) {
        mutableStateOf(false)
    }
    var hasMoreMediaItems by remember(mode) {
        mutableStateOf(true)
    }
    var mediaPageOffset by remember(mode) {
        mutableStateOf(0)
    }
    var mediaLoadGeneration by remember(mode) {
        mutableStateOf(0)
    }
    val currentSelection = remember {
        mutableStateListOf<MediaPickerItem>()
    }
    val pickerScope = rememberCoroutineScope()
    // In-memory only — swap for a persisted store (e.g. backed by Room) if recent files
    // need to survive process death.
    val recentFiles = remember {
        mutableStateListOf<MediaPickerItem>()
    }
    var showCameraCapture by remember {
        mutableStateOf(false)
    }
    var showGalleryAsFilePicker by remember {
        mutableStateOf(false)
    }
    var pendingTabChange by remember {
        mutableStateOf<MediaPickerTab?>(null)
    }

    val mediaListState = rememberLazyListState()

    fun selectUncompressedFiles(
        files: List<MediaPickerItem>,
        saveToRecents: Boolean
    ) {
        if (files.isEmpty()) return

        pickerScope.launch {
            if (saveToRecents) {
                val newRecents = files.filter { item -> item.type == MediaPickerItemType.FILE }
                recentFiles.addAll(0, newRecents)
            }

            onSelect(
                MediaPickerResult(
                    type = MediaPickerResultType.FILE, // everything come from here will be send as file
                    items = files
                )
            )
        }
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionState = context.mediaPermissionState(mediaMode)
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val fileItems = context.fileItemsFromUris(uris)
        selectUncompressedFiles(
            files = fileItems,
            saveToRecents = true
        )
    }

    LaunchedEffect(selectedItems) {
        currentSelection.replaceWith(
            selectedItems.filter { item -> item.type != MediaPickerItemType.FILE }
        )
    }

    fun switchTab(tab: MediaPickerTab, clearSelection: Boolean) {
        selectedTab = tab
        showAlbumPicker = false

        if (clearSelection) {
            currentSelection.clear()
            onSelectionCleared()
        }
    }

    fun requestTabChange(tab: MediaPickerTab) {
        if (tab == selectedTab) return

        if (currentSelection.isNotEmpty()) {
            pendingTabChange = tab
            return
        }

        switchTab(tab = tab, clearSelection = false)
    }

    // Loads the album list only when the tab/permission/mode actually change - not on every
    // album selection, since re-scanning every bucket on each tap was wasted work.
    LaunchedEffect(selectedTab, permissionState.hasAccess, mediaMode) {
        if (selectedTab != MediaPickerTab.GALLERY) return@LaunchedEffect

        permissionState = context.mediaPermissionState(mediaMode)

        if (!permissionState.hasAccess) {
            mediaPermissionLauncher.launch(requiredMediaPermissions(mediaMode))
            return@LaunchedEffect
        }

        val loadedAlbums = withContext(Dispatchers.IO) {
            queryMediaPickerAlbums(context, mediaMode)
        }
        albumList = loadedAlbums
        val resolvedAlbum = when {
            loadedAlbums.isEmpty() -> null
            selectedAlbum == null -> loadedAlbums.first()
            else -> loadedAlbums.firstOrNull { it.id == selectedAlbum?.id }
                ?: loadedAlbums.first()
        }
        if (resolvedAlbum != selectedAlbum) {
            selectedAlbum = resolvedAlbum
        }
    }

    // Loads the first page of the selected album/gallery. Paginated further pages are fetched
    // on demand by loadMoreMediaItems() as the user scrolls.
    LaunchedEffect(selectedTab, permissionState.hasAccess, mediaMode, selectedAlbum?.id) {
        if (selectedTab != MediaPickerTab.GALLERY || !permissionState.hasAccess) {
            return@LaunchedEffect
        }

        mediaLoadGeneration++
        val generation = mediaLoadGeneration
        isMediaLoading = true
        hasMoreMediaItems = true

        val firstPage = withContext(Dispatchers.IO) {
            queryMediaPickerItemsPage(
                context = context,
                mode = mediaMode,
                albumId = selectedAlbum?.id,
                offset = 0
            )
        }

        if (generation != mediaLoadGeneration) return@LaunchedEffect

        mediaItems = firstPage.items
        hasMoreMediaItems = firstPage.hasMore
        mediaPageOffset = firstPage.items.size
        isMediaLoading = false
    }

    fun loadMoreMediaItems() {
        if (selectedTab != MediaPickerTab.GALLERY || !permissionState.hasAccess) return
        if (isMediaLoading || isLoadingMoreMedia || !hasMoreMediaItems) return

        val generation = mediaLoadGeneration
        val offset = mediaPageOffset
        isLoadingMoreMedia = true

        pickerScope.launch {
            val nextPage = withContext(Dispatchers.IO) {
                queryMediaPickerItemsPage(
                    context = context,
                    mode = mediaMode,
                    albumId = selectedAlbum?.id,
                    offset = offset
                )
            }

            if (generation == mediaLoadGeneration) {
                mediaItems = mediaItems + nextPage.items
                hasMoreMediaItems = nextPage.hasMore
                mediaPageOffset = offset + nextPage.items.size
            }

            isLoadingMoreMedia = false
        }
    }

    if (showCameraCapture) {
        MediaPickerCameraCapturePage(
            onDismiss = {
                showCameraCapture = false
            },
            onImageCaptured = { capturedItem ->
                selectedTab = MediaPickerTab.GALLERY
                mediaItems =
                    listOf(capturedItem) + mediaItems.filterNot { it.uri == capturedItem.uri }
                currentSelection.selectCaptured(capturedItem, maxSelection)
                showCameraCapture = false
            }
        )
        return
    }

    if (showGalleryAsFilePicker) {
        PickGalleryAsFileContent(
            maxSelection = maxSelection,
            onDismiss = {
                showGalleryAsFilePicker = false
            },
            onDone = { selectedFiles ->
                showGalleryAsFilePicker = false
                selectUncompressedFiles(
                    files = selectedFiles,
                    saveToRecents = false
                )
            }
        )
        return
    }

    pendingTabChange?.let { targetTab ->
        AppDialogFrame(onDismiss = { pendingTabChange = null }) {
            AppDialogTitle(text = "Clear selected items?")

            8.HeightBox()

            AppDialogMessage(text = "Changing picker type will remove your current selection.")

            18.HeightBox()

            AppDialogTextActions(
                onCancel = { pendingTabChange = null },
                onConfirm = {
                    CustomHaptic.doubleLightImpact(context)
                    switchTab(tab = targetTab, clearSelection = true)
                    pendingTabChange = null
                },
                confirmText = "Continue",
                confirmTextColor = CustomColor.accentBlue
            )
        }
    }
    val pickerHeaderHeight = 62.dp
    val permissionHeaderHeight = 60.dp
    val handleAndPadding = 15.dp

    val headerGradientHeight = pickerHeaderHeight + handleAndPadding +
            if (permissionState.isLimited) permissionHeaderHeight else 0.dp
    val headerGradientColor = theme.background2
    val galleryLabel = selectedAlbum?.displayName ?: "Gallery"
    val canShowAlbumPicker = permissionState.hasAccess && albumList.isNotEmpty()

    CustomBottomSheetPlate(
        onDismissRequest = onDismiss,
        sheetHeightFraction = sheetHeightFraction,
        modifier = modifier,
        enableDraggable = !showAlbumPicker,
        includeDragHandle = false,
        containerColor = theme.background2,
        contentPadding = PaddingValues(
            0.dp
        ),
        applyNavigationBarPadding = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                MediaPickerTab.GALLERY -> {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when {
                            !permissionState.hasAccess -> {
                                PermissionRequiredContent(
                                    onGrantClick = {
                                        mediaPermissionLauncher.launch(
                                            requiredMediaPermissions(
                                                mediaMode
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            isMediaLoading -> {
                                MediaPickerLoadingContent(
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            else -> {
                                MediaPickerContent(
                                    listState = mediaListState,
                                    mediaItems = mediaItems,
                                    selectedItems = currentSelection,
                                    maxSelection = maxSelection,
                                    onItemClick = { item ->
                                        if (isSinglePickerMode) {
                                            CustomHaptic.doubleLightImpact(context)

                                            onSelect(
                                                MediaPickerResult(
                                                    type = MediaPickerResultType.MEDIA,
                                                    items = listOf(item)
                                                )
                                            )
                                        } else {
                                            currentSelection.toggle(item, maxSelection)
                                        }
                                    },
                                    onCameraClick = {
                                        showCameraCapture = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    showCameraPreviewTile = includeCameraPreview,
                                    isSinglePickerMode = isSinglePickerMode,

                                    // add this param in MediaPickerContent
                                    contentPadding = PaddingValues(
                                        top = headerGradientHeight,
                                        bottom = 10.dp + WindowInsets.navigationBars
                                            .asPaddingValues()
                                            .calculateBottomPadding()
                                    ),
                                    isLoadingMore = isLoadingMoreMedia,
                                    onLoadMore = ::loadMoreMediaItems
                                )
                            }
                        }
                    }
                }

                MediaPickerTab.FILE -> {
                    FilePickerContent(
                        recentFiles = recentFiles,
                        onPickLocalFiles = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onPickGalleryFiles = {
                            if (permissionState.hasAccess) {
                                showGalleryAsFilePicker = true
                            } else {
                                mediaPermissionLauncher.launch(requiredMediaPermissions(mediaMode))
                            }
                        },
                        onRecentFileClick = { file ->
                            selectUncompressedFiles(
                                files = listOf(file),
                                saveToRecents = true
                            )
                        },
                        contentPadding = PaddingValues(
                            top = headerGradientHeight + 15.dp,
                            bottom = 10.dp + WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(headerGradientHeight)
                    .zIndex(1f)
            ) {
                TopGradient(
                    modifier = Modifier.fillMaxSize(),
                    color = headerGradientColor
                )

//                Box(
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .fillMaxWidth()
//                        .height(1.dp)
//                        .background(theme.bottomSheetInputDivider.copy(alpha = 0.28f))
//                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            ) {
                10.HeightBox()
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(54.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = theme.bottomSheetInputDivider.copy(alpha = 0.7f))
                )
                MediaPickerHeader(
                    selectionCount = currentSelection.size,
                    mode = mode,
                    selectedTab = selectedTab,
                    galleryLabel = galleryLabel,
                    showGalleryAlbumTrigger = canShowAlbumPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pickerHeaderHeight),
                    onTabSelected = { tab ->
                        requestTabChange(tab)
                    },
                    onGalleryAlbumClick = {
                        if (selectedTab == MediaPickerTab.GALLERY && canShowAlbumPicker) {
                            CustomHaptic.doubleLightImpact(context)
                            showAlbumPicker = !showAlbumPicker
                        }
                    },
                    onSend = {
                        CustomHaptic.doubleLightImpact(context)
                        onSelect(
                            currentSelection.toList().toPickerResult(
                                fallbackType = if (selectedTab == MediaPickerTab.FILE) {
                                    MediaPickerResultType.FILE
                                } else {
                                    MediaPickerResultType.MEDIA
                                }
                            )
                        )
                    }
                )
                if (permissionState.isLimited) {
                    LimitedAccessBanner(
                        height = permissionHeaderHeight,
                        onManageAccess = {
                            mediaPermissionLauncher.launch(
                                requiredMediaPermissions(
                                    mediaMode
                                )
                            )
                        }
                    )
                }
            }

            if (
                showAlbumPicker &&
                selectedTab == MediaPickerTab.GALLERY &&
                canShowAlbumPicker
            ) {
                MediaPickerAlbumScrim(
                    onDismiss = { showAlbumPicker = false },
                    modifier = Modifier.zIndex(2.5f)
                )

                AlbumPickerDropdown(
                    albums = albumList,
                    selectedAlbum = selectedAlbum,
                    onAlbumSelected = { album ->
                        CustomHaptic.doubleLightImpact(context)
                        selectedAlbum = album
                        showAlbumPicker = false
                        pickerScope.launch {
                            mediaListState.scrollToItem(0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = handleAndPadding + pickerHeaderHeight)
                        .zIndex(3f)
                        .padding(horizontal = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun LimitedAccessBanner(
    onManageAccess: () -> Unit,
    height: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 21.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "You've limited this app's access to your photos.",
            color = theme.textSecondary,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.W500,
            fontFamily = OpenAISans,
            modifier = Modifier.weight(1f),
            letterSpacing = (-0.4f).sp
        )

        12.WidthBox()

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF2674F4))
                .clickable(onClick = onManageAccess)
                .padding(horizontal = 17.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Manage Access",
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.W700,
                fontFamily = OpenAISans,
                maxLines = 1,
                letterSpacing = (-0.4f).sp
            )
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.gallery),
            contentDescription = null,
            tint = theme.cardRowIconsColor,
            modifier = Modifier.size(42.dp)
        )

        12.HeightBox()

        Text(
            text = "Photo access is needed to show your recent media.",
            color = theme.textSecondary,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W500,
            fontFamily = OpenAISans,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.4f).sp
        )

        18.HeightBox()

        PickerPrimaryButton(
            text = "Allow Access",
            icon = painterResource(R.drawable.add),
            onClick = onGrantClick
        )
    }
}


@Composable
internal fun PickerPrimaryButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(theme.mainText)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = theme.background,
            modifier = Modifier.size(18.dp)
        )

        8.WidthBox()

        Text(
            text = text,
            color = theme.background,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.W600,
            fontFamily = OpenAISans,
            maxLines = 1,
            letterSpacing = (-0.4f).sp
        )
    }
}


@Composable
private fun MediaPickerLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = theme.mainText,
            strokeWidth = 2.dp,
            modifier = Modifier.size(30.dp)
        )
    }
}


private fun MutableList<MediaPickerItem>.replaceWith(items: List<MediaPickerItem>) {
    clear()
    addAll(items.distinctBy { it.uri })
}

private fun MutableList<MediaPickerItem>.toggle(
    item: MediaPickerItem,
    maxSelection: Int
) {
    val existingIndex = indexOfFirst { it.uri == item.uri }

    if (existingIndex >= 0) {
        removeAt(existingIndex)
        return
    }

    if (maxSelection == 1) {
        clear()
        add(item)
        return
    }

    if (size >= maxSelection) return

    add(item)
}

private fun MutableList<MediaPickerItem>.selectCaptured(
    item: MediaPickerItem,
    maxSelection: Int
) {
    removeAll { it.uri == item.uri }

    if (maxSelection == 1) {
        clear()
    } else {
        while (size >= maxSelection && isNotEmpty()) {
            removeAt(lastIndex)
        }
    }

    add(0, item)
}

internal fun Context.loadVideoThumbnail(uri: Uri): Bitmap? {
    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                retriever.getScaledFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    320,
                    320
                )
            } else {
                retriever.getFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
            }
        } finally {
            retriever.release()
        }
    }.getOrNull()
}
