package com.kblack.offlinemap.presentation.screen.home.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    smallFontSize: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = Color(0xFF32628D),
) {
    val fontSize =
        if (smallFontSize) MaterialTheme.typography.bodyMedium.fontSize
        else MaterialTheme.typography.bodyLarge.fontSize
    CompositionLocalProvider {
        ProvideTextStyle(
            value =
                TextStyle(
                    fontSize = fontSize,
                    lineHeight = fontSize * if (smallFontSize) 1.4f else 1.5f,
                    color = textColor,
                    letterSpacing = 0.2.sp,
                )
        ) {
            RichText(
                modifier = modifier,
                style =
                    RichTextStyle(
                        codeBlockStyle =
                            CodeBlockStyle(
                                textStyle =
                                    TextStyle(
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.4f,
                                    )
                            ),
                        stringStyle =
                            RichTextStringStyle(linkStyle = TextLinkStyles(style = SpanStyle(color = linkColor))),
                    ),
            ) {
                Markdown(content = text)
            }
        }
    }
}