package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.kblack.offlinemap.models.PlaceSearch

private fun buildHighlightedNameText(fullText: String, query: String): AnnotatedString {
    if (query.isBlank() || fullText.isBlank()) return AnnotatedString(fullText)

    val highlightStyle = SpanStyle(
        background = Color(0xFFFFEB3B),
        color = Color.Black
    )

    return buildAnnotatedString {
        var currentIndex = 0
        val queryLength = query.length

        while (currentIndex < fullText.length) {
            val matchIndex = fullText.indexOf(
                string = query,
                startIndex = currentIndex,
                ignoreCase = true
            )

            if (matchIndex < 0) {
                append(fullText.substring(currentIndex))
                break
            }

            if (matchIndex > currentIndex) {
                append(fullText.substring(currentIndex, matchIndex))
            }

            withStyle(highlightStyle) {
                append(fullText.substring(matchIndex, matchIndex + queryLength))
            }

            currentIndex = matchIndex + queryLength
        }
    }
}

@Composable
fun FloatingSearchBar(
    searchQuery: String,
    searchResults: List<PlaceSearch>,
    isSearching: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onLocationSelected: (PlaceSearch) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxSearchWidth = maxWidth
        val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            placeholder = { Text("Search for a location...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = surfaceContainerColor,
                unfocusedContainerColor = surfaceContainerColor,
                focusedTextColor = onSurfaceColor,
                unfocusedTextColor = onSurfaceColor,
                focusedLabelColor = onSurfaceColor,
                unfocusedLabelColor = onSurfaceColor.copy(alpha = 0.6f),
                cursorColor = onSurfaceColor,
                focusedBorderColor = onSurfaceColor,
                unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.3f)
            ),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSearchQueryChanged("")
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        )

        if (isSearching || searchQuery.length >= 2) {
            val density = LocalDensity.current
            val yOffsetPx = remember(density) { with(density) { 60.dp.roundToPx() } }

            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, yOffsetPx),
                properties = PopupProperties(focusable = false)
            ) {
                Card(
                    modifier = Modifier
                        .width(maxSearchWidth)
                        .heightIn(max = 280.dp)
                        .padding(top = 2.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceContainerColor
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = onSurfaceColor,
                                modifier = Modifier
                            )
                        }
                    } else {
                        if (searchResults.isEmpty()) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "No results found",
//                                    color = onSurfaceColor.copy(alpha = 0.7f),
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
//                            }
                            Box() {}
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(searchResults) { result ->
                                    TextButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            onLocationSelected(result)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 12.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(
                                                text = buildHighlightedNameText(
                                                    result.name,
                                                    searchQuery
                                                ),
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = onSurfaceColor
                                                )
                                            )
                                            val subtitleParts = listOfNotNull(
                                                result.city.takeIf { it.isNotBlank() },
                                                result.state.takeIf { it.isNotBlank() },
                                                result.country.takeIf { it.isNotBlank() }
                                            )
                                            val subtitle = subtitleParts.joinToString(", ")

                                            if (subtitle.isNotBlank()) {
                                                Row(modifier = Modifier) {
                                                    Text(
                                                        text = subtitle,
                                                        color = onSurfaceColor.copy(alpha = 0.7f),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                            Row(modifier = Modifier) {
                                                Text(
                                                    text = "Lat: ${result.lat}, Lon: ${result.lng}",
                                                    color = onSurfaceColor.copy(alpha = 0.7f),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            HorizontalDivider(
                                                modifier = Modifier.padding(top = 4.dp),
                                                color = onSurfaceColor.copy(alpha = 0.1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}