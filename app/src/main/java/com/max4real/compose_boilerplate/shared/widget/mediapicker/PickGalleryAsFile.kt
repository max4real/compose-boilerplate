package com.max4real.compose_boilerplate.shared.widget.mediapicker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.shared.widget.mediapicker.gallery_widget.MediaPickerPlainGridContent
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PickGalleryAsFileContent(
    maxSelection: Int,
    onDismiss: () -> Unit,
    onDone: (List<MediaPickerItem>) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        PickGalleryAsFileScreen(
            maxSelection = maxSelection,
            onDismiss = onDismiss,
            onDone = onDone
        )
    }
}

@Composable
private fun PickGalleryAsFileScreen(
    maxSelection: Int,
    onDismiss: () -> Unit,
    onDone: (List<MediaPickerItem>) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    val pickerScope = rememberCoroutineScope()
    val selectedItems = remember {
        mutableStateListOf<MediaPickerItem>()
    }
    var albumList by remember {
        mutableStateOf<List<MediaPickerAlbum>>(emptyList())
    }
    var selectedAlbum by remember {
        mutableStateOf<MediaPickerAlbum?>(null)
    }
    var showAlbumPicker by remember {
        mutableStateOf(false)
    }
    var albumMediaItems by remember {
        mutableStateOf<List<MediaPickerItem>>(emptyList())
    }
    var isAlbumMediaLoading by remember {
        mutableStateOf(true)
    }
    var isLoadingMoreAlbumMedia by remember {
        mutableStateOf(false)
    }
    var hasMoreAlbumMedia by remember {
        mutableStateOf(true)
    }
    var albumMediaPageOffset by remember {
        mutableStateOf(0)
    }
    var albumMediaLoadGeneration by remember {
        mutableStateOf(0)
    }
    val bottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding() + 16.dp
    val canShowAlbumPicker = albumList.isNotEmpty()
    val galleryLabel = selectedAlbum?.displayName ?: "Gallery"

    // Loads the album list once, on entry - not on every album switch.
    LaunchedEffect(Unit) {
        val loadedAlbums = withContext(Dispatchers.IO) {
            queryMediaPickerAlbums(context, MediaPickerMode.MEDIA)
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

    // Loads the first page for the selected album/gallery. Further pages are fetched on demand
    // by loadMoreAlbumMediaItems() as the user scrolls.
    LaunchedEffect(selectedAlbum?.id) {
        albumMediaLoadGeneration++
        val generation = albumMediaLoadGeneration
        isAlbumMediaLoading = true
        hasMoreAlbumMedia = true

        val firstPage = withContext(Dispatchers.IO) {
            queryMediaPickerItemsPage(
                context = context,
                mode = MediaPickerMode.MEDIA,
                albumId = selectedAlbum?.id,
                offset = 0
            )
        }

        if (generation != albumMediaLoadGeneration) return@LaunchedEffect

        albumMediaItems = firstPage.items
        hasMoreAlbumMedia = firstPage.hasMore
        albumMediaPageOffset = firstPage.items.size
        isAlbumMediaLoading = false
    }

    fun loadMoreAlbumMediaItems() {
        if (isAlbumMediaLoading || isLoadingMoreAlbumMedia || !hasMoreAlbumMedia) return

        val generation = albumMediaLoadGeneration
        val offset = albumMediaPageOffset
        isLoadingMoreAlbumMedia = true

        pickerScope.launch {
            val nextPage = withContext(Dispatchers.IO) {
                queryMediaPickerItemsPage(
                    context = context,
                    mode = MediaPickerMode.MEDIA,
                    albumId = selectedAlbum?.id,
                    offset = offset
                )
            }

            if (generation == albumMediaLoadGeneration) {
                albumMediaItems = albumMediaItems + nextPage.items
                hasMoreAlbumMedia = nextPage.hasMore
                albumMediaPageOffset = offset + nextPage.items.size
            }

            isLoadingMoreAlbumMedia = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background2)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GalleryAsFileTopBar(
                selectedCount = selectedItems.size,
                galleryLabel = galleryLabel,
                canShowAlbumPicker = canShowAlbumPicker,
                onAlbumClick = {
                    if (canShowAlbumPicker) {
                        showAlbumPicker = !showAlbumPicker
                    }
                },
                onBack = onDismiss,
                onDone = {
                    onDone(selectedItems.toList())
                }
            )

            when {
                isAlbumMediaLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = theme.mainText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                else -> {
                    MediaPickerPlainGridContent(
                        mediaItems = albumMediaItems,
                        selectedItems = selectedItems,
                        maxSelection = maxSelection,
                        onItemClick = { item ->
                            selectedItems.toggle(item, maxSelection)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = bottomPadding),
                        isLoadingMore = isLoadingMoreAlbumMedia,
                        onLoadMore = ::loadMoreAlbumMediaItems
                    )
                }
            }
        }

        if (showAlbumPicker && canShowAlbumPicker) {
            MediaPickerAlbumScrim(
                onDismiss = { showAlbumPicker = false },
                modifier = Modifier.zIndex(1f)
            )

            AlbumPickerDropdown(
                albums = albumList,
                selectedAlbum = selectedAlbum,
                onAlbumSelected = { album ->
                    selectedAlbum = album
                    showAlbumPicker = false
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .offset(y = 58.dp)
                    .zIndex(2f)
            )
        }
    }
}

@Composable
private fun GalleryAsFileTopBar(
    selectedCount: Int,
    galleryLabel: String,
    canShowAlbumPicker: Boolean,
    onAlbumClick: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(62.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GalleryTopIconButton(
            onClick = onBack
        )

        12.WidthBox()

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    enabled = canShowAlbumPicker,
                    onClick = onAlbumClick
                )
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = galleryLabel,
                color = theme.mainText,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.W700,
                fontFamily = OpenAISans,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
                letterSpacing = (-0.4f).sp
            )

            if (canShowAlbumPicker) {
                Icon(
                    painter = painterResource(R.drawable.arrow_down),
                    contentDescription = null,
                    tint = theme.mainText,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(14.dp)
                )
            }
        }

        if (selectedCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFF2674F4))
                    .clickable(onClick = onDone)
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Send",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = OpenAISans,
                    maxLines = 1,
                    letterSpacing = (-0.4f).sp
                )
            }
        }
    }
}

@Composable
private fun GalleryTopIconButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(theme.appBarIconButton)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = theme.rippleColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_left),
            contentDescription = "Back",
            tint = theme.appBarIcon,
            modifier = Modifier.size(22.dp)
        )
    }
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
