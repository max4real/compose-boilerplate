package com.xsphere.roomchatandroid.shared.widget.spoiler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsphere.roomchatandroid.shared.util.parseSpoilerMarkup
import com.xsphere.roomchatandroid.ui.theme.CustomColor
import com.xsphere.roomchatandroid.ui.theme.OpenAISans
import com.xsphere.roomchatandroid.ui.theme.theme
import kotlin.math.roundToInt

private const val OTP_PREVIEW =
    "Login code: >!284611!<. Do not give this code to anyone, even if they say they are from Room."
private const val OTP_TAIL_PREVIEW =
    "Hey, your verification is on the way. It should land in a second, the one you asked for is >!903472!< okay"
private const val OTP_BUBBLE =
    "Your one-time password is >!519204!< and it expires in 5 minutes."
private const val WRAPPED_TEXT =
    "Recovery phrase: >!aurora ember violet pine cascade lantern harbor meadow!<, keep it somewhere safe and never share it with anyone."
private const val PARAGRAPH_TEXT =
    "The account is held under >!alpha bravo\ncharlie delta!<, and the branch code sits on the second line."
private const val ESCAPED_TEXT =
    "The shell test was \\>!not a spoiler\\!< but >!this one is!< for sure."

@Composable
fun SpoilerDemoScreen(
    modifier: Modifier = Modifier
) {
    var spoilerStyle by remember { mutableStateOf(SpoilerStyle()) }
    var isObscured by remember { mutableStateOf(true) }
    var isBubbleObscured by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "Spoiler V2",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Hidden glyphs are painted transparent, the dust is drawn on top",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DemoCard(title = "Chat list preview — app theme") {
                PreviewRow(
                    name = "Room",
                    time = "12:39 PM",
                    text = OTP_PREVIEW.parseSpoilerMarkup().text,
                    background = theme.background,
                    nameColor = theme.chatItemName,
                    messageColor = theme.chatItemMessage,
                    timeColor = theme.chatItemTime,
                    spoilerStyle = spoilerStyle,
                    isObscured = isObscured
                )
                PreviewRow(
                    name = "Telegram",
                    time = "12:38 PM",
                    text = OTP_TAIL_PREVIEW.parseSpoilerMarkup().text,
                    background = theme.background,
                    nameColor = theme.chatItemName,
                    messageColor = theme.chatItemMessage,
                    timeColor = theme.chatItemTime,
                    spoilerStyle = spoilerStyle,
                    isObscured = isObscured
                )
            }

            DemoCard(title = "Forced dark") {
                PreviewRow(
                    name = "Room",
                    time = "12:39 PM",
                    text = OTP_PREVIEW.parseSpoilerMarkup().text,
                    background = Color(0xFF0F1014),
                    nameColor = Color(0xFFF2F3F5),
                    messageColor = Color(0xFF9BA1A6),
                    timeColor = Color(0xFF6E757C),
                    spoilerStyle = spoilerStyle,
                    isObscured = isObscured
                )
            }

            DemoCard(title = "Forced light") {
                PreviewRow(
                    name = "Room",
                    time = "12:39 PM",
                    text = OTP_PREVIEW.parseSpoilerMarkup().text,
                    background = Color(0xFFFFFFFF),
                    nameColor = Color(0xFF111315),
                    messageColor = Color(0xFF6B7075),
                    timeColor = Color(0xFF9AA0A6),
                    spoilerStyle = spoilerStyle,
                    isObscured = isObscured
                )
            }

            DemoCard(title = "Tap the dust to reveal") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(theme.receiverBubbleColor)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    SpoilerTextV2(
                        text = OTP_BUBBLE.parseSpoilerMarkup().text,
                        spoilerRanges = remember { OTP_BUBBLE.parseSpoilerMarkup().ranges },
                        color = theme.receiverBubbleTextColor,
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 21.sp,
                            fontFamily = OpenAISans,
                            fontWeight = FontWeight.W400
                        ),
                        isObscured = isBubbleObscured,
                        spoilerStyle = spoilerStyle,
                        onSpoilerTap = { isBubbleObscured = false }
                    )
                }

                Button(onClick = { isBubbleObscured = true }) {
                    Text(text = "Hide again")
                }
            }

            DemoCard(title = "Preview text only") {
                SpoilerTextV2(
                    text = OTP_PREVIEW.parseSpoilerMarkup().text,
                    spoilerRanges = remember { OTP_PREVIEW.parseSpoilerMarkup().ranges },
                    isObscured = isObscured,
                    color = theme.chatItemMessage,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontFamily = OpenAISans,
                        letterSpacing = (-0.4).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    spoilerStyle = spoilerStyle
                )
            }

            DemoCard(title = "Multiline — wrapped range") {
                SpoilerTextV2(
                    text = WRAPPED_TEXT.parseSpoilerMarkup().text,
                    spoilerRanges = remember { WRAPPED_TEXT.parseSpoilerMarkup().ranges },
                    isObscured = isObscured,
                    color = theme.mainText,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontFamily = OpenAISans,
                        letterSpacing = (-0.4).sp
                    ),
                    spoilerStyle = spoilerStyle
                )
            }

            DemoCard(title = "Multiline — hard line break inside range") {
                SpoilerTextV2(
                    text = PARAGRAPH_TEXT.parseSpoilerMarkup().text,
                    spoilerRanges = remember { PARAGRAPH_TEXT.parseSpoilerMarkup().ranges },
                    isObscured = isObscured,
                    color = theme.mainText,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontFamily = OpenAISans,
                        letterSpacing = (-0.4).sp
                    ),
                    spoilerStyle = spoilerStyle
                )
            }

            DemoCard(title = "Escaped markers") {
                SpoilerTextV2(
                    text = ESCAPED_TEXT.parseSpoilerMarkup().text,
                    spoilerRanges = remember { ESCAPED_TEXT.parseSpoilerMarkup().ranges },
                    isObscured = isObscured,
                    color = theme.mainText,
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontFamily = OpenAISans,
                        letterSpacing = (-0.4).sp
                    ),
                    spoilerStyle = spoilerStyle
                )
            }

            DemoCard(title = "Tuning") {
                TuningSlider(
                    label = "Density",
                    value = spoilerStyle.particleDensity,
                    range = 0.05f..1.2f
                ) { spoilerStyle = spoilerStyle.copy(particleDensity = it) }

                TuningSlider(
                    label = "Speed",
                    value = spoilerStyle.speed,
                    range = 0.2f..3f
                ) { spoilerStyle = spoilerStyle.copy(speed = it) }

                TuningSlider(
                    label = "Dot size",
                    value = spoilerStyle.maxRadius.value,
                    range = 0.4f..2.5f
                ) { spoilerStyle = spoilerStyle.copy(maxRadius = it.dp) }

                TuningSlider(
                    label = "Drift",
                    value = spoilerStyle.drift.value,
                    range = 0f..16f
                ) { spoilerStyle = spoilerStyle.copy(drift = it.dp) }

                TuningSlider(
                    label = "Lifetime",
                    value = spoilerStyle.maxLifetime,
                    range = 0.4f..3f
                ) { spoilerStyle = spoilerStyle.copy(maxLifetime = it) }

                TuningSlider(
                    label = "Feather",
                    value = spoilerStyle.feather.value,
                    range = 0f..8f
                ) { spoilerStyle = spoilerStyle.copy(feather = it.dp) }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { spoilerStyle = SpoilerStyle() }
                ) {
                    Text(text = "Reset")
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { isObscured = !isObscured }
            ) {
                Text(text = if (isObscured) "Reveal all" else "Hide all")
            }
        }
    }
}

@Composable
private fun DemoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
private fun PreviewRow(
    name: String,
    time: String,
    text: String,
    background: Color,
    nameColor: Color,
    messageColor: Color,
    timeColor: Color,
    spoilerStyle: SpoilerStyle,
    isObscured: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(CustomColor.bubbleBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                fontFamily = OpenAISans
            )
        }

        Spacer(modifier = Modifier.width(15.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = nameColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = OpenAISans,
                    letterSpacing = (-0.4).sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = time,
                    color = timeColor,
                    fontSize = 14.sp,
                    fontFamily = OpenAISans,
                    letterSpacing = (-0.4).sp
                )
            }

            Spacer(modifier = Modifier.size(3.dp))

            SpoilerTextV2(
                text = remember(text) { text.parseSpoilerMarkup().text },
                spoilerRanges = remember(text) { text.parseSpoilerMarkup().ranges },
                color = messageColor,
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontFamily = OpenAISans,
                    fontWeight = FontWeight.W400,
                    letterSpacing = (-0.4).sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                isObscured = isObscured,
                spoilerStyle = spoilerStyle
            )
        }
    }
}

@Composable
private fun TuningSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = "$label  ${(value * 100).roundToInt() / 100f}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            valueRange = range,
            onValueChange = onValueChange
        )
    }
}
