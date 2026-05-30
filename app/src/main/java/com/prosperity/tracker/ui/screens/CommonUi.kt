package com.prosperity.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prosperity.tracker.ui.theme.IncomeGreen
import com.prosperity.tracker.ui.theme.OnSurface
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary
import com.prosperity.tracker.util.formatRupiah

/** Large screen heading. */
@Composable
fun ScreenTitle(text: String) {
    Text(
        text,
        color = OnSurface,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

/** Circular badge showing an emoji on a tinted background. */
@Composable
fun EmojiBadge(emoji: String, size: Int = 44, background: Color = Primary.copy(alpha = 0.10f)) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size * 0.45f).sp)
    }
}

/** A transaction list row used by Home and History. */
@Composable
fun TransactionRow(
    emoji: String,
    title: String,
    subtitle: String,
    amount: Long,
    isIncome: Boolean,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = if (trailing != null) 4.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmojiBadge(emoji)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = OnSurfaceVariant, fontSize = 13.sp)
            }
        }
        Text(
            (if (isIncome) "+" else "-") + formatRupiah(amount),
            color = if (isIncome) IncomeGreen else OnSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        if (trailing != null) trailing()
    }
}
