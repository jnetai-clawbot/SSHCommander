package com.jnetaol.sshcommander.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.sshcommander.ui.theme.*

@Composable
fun GlowButton(
    text: String, icon: ImageVector? = null, onClick: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, glowColor: Color = SCPrimary
) {
    val transition = rememberInfiniteTransition(label = "glow")
    val alpha by transition.animateFloat(0.4f, 0.8f, infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse), label = "a")
    Button(onClick = onClick, enabled = enabled, modifier = modifier.shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = glowColor.copy(alpha = alpha), spotColor = glowColor.copy(alpha = alpha)),
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = glowColor.copy(alpha = 0.15f), disabledContainerColor = glowColor.copy(alpha = 0.05f), disabledContentColor = SCTextMuted),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)) {
        if (icon != null) { Icon(icon, null, Modifier.size(20.dp), tint = glowColor); Spacer(Modifier.width(8.dp)) }
        Text(text, color = glowColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun NeonCard(modifier: Modifier = Modifier, borderColor: Color = SCPrimary.copy(alpha = 0.3f), content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SCCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), content = content)
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = SCTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (action != null && onAction != null) TextButton(onAction) { Text(action, color = SCSecondary, fontSize = 14.sp) }
    }
}

@Composable
fun StatusBadge(text: String, color: Color = SCPrimary, modifier: Modifier = Modifier) {
    Box(modifier.background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.1f))), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, Modifier.size(64.dp), tint = SCTextMuted.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text(title, color = SCTextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = SCTextMuted, fontSize = 14.sp)
    }
}

@Composable
fun PulseDot(color: Color = SCSuccess, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(0.8f, 1.4f, infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse), label = "s")
    val alpha by transition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse), label = "a")
    Box(modifier.size(10.dp).scale(scale).alpha(alpha).clip(CircleShape).background(color))
}

@Composable
fun NeonTextField(
    value: String, onValueChange: (String) -> Unit, placeholder: String = "",
    modifier: Modifier = Modifier, leadingIcon: ImageVector? = null,
    glowColor: Color = SCPrimary, singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = modifier, placeholder = { Text(placeholder, color = SCTextMuted) },
        leadingIcon = if (leadingIcon != null) {{ Icon(leadingIcon, null, tint = SCTextMuted) }} else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = SCTextPrimary, unfocusedTextColor = SCTextPrimary,
            focusedBorderColor = glowColor.copy(alpha = 0.5f), unfocusedBorderColor = SCSurfaceVariant,
            cursorColor = glowColor, focusedContainerColor = SCSurface,
            unfocusedContainerColor = SCSurface
        ),
        shape = RoundedCornerShape(12.dp), singleLine = singleLine
    )
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color = SCPrimary, modifier: Modifier = Modifier) {
    NeonCard(modifier = modifier, borderColor = color.copy(alpha = 0.3f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(24.dp), tint = color)
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, color = SCTextMuted, fontSize = 11.sp)
        }
    }
}
