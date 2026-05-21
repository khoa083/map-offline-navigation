package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.kblack.offlinemap.models.PlaceSearch

@Composable
fun FloatingSearchBar(
    searchQuery: String,
    searchResults: List<PlaceSearch>,
    onSearchQueryChanged: (String) -> Unit,
    onLocationSelected: (PlaceSearch) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxSearchWidth = maxWidth

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Tìm kiếm địa điểm...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
            ),
            singleLine = true
        )

        if (searchResults.isNotEmpty()) {
            val density = LocalDensity.current
            val yOffsetPx = remember(density) { with(density) { 70.dp.roundToPx() } }

            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, yOffsetPx),
                properties = PopupProperties(focusable = false)
            ) {
                Card(
                    modifier = Modifier
                        .width(maxSearchWidth)
                        .heightIn(max = 280.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchResults) { result ->
                            TextButton(
                                onClick = { onLocationSelected(result) },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(text = result.name, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}