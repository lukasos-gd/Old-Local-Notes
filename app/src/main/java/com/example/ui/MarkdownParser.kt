package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MarkdownParser {
    fun parse(text: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                if (text.startsWith("**", i)) {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                        continue
                    }
                }
                if (text.startsWith("*", i)) {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                        continue
                    }
                }
                append(text[i])
                i++
            }
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Column(modifier = modifier) {
        val lines = text.lines()
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("# ") -> {
                    val cleanLine = line.removePrefix("# ").trim()
                    Text(
                        text = cleanLine,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    val cleanLine = line.removePrefix("## ").trim()
                    Text(
                        text = cleanLine,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                    val cleanLine = line.replaceFirst(Regex("^[-*•]\\s*"), "").trim()
                    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                        Text(
                            text = "•  ",
                            style = style.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = MarkdownParser.parse(cleanLine),
                            style = style
                        )
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        text = MarkdownParser.parse(line),
                        style = style,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
