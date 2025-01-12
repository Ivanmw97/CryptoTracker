package com.ivandev.cryptotracker.crypto.presentation.coin_list.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
) {
    val items = listOf("Home", "Favorites")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Favorite
    )
    val contentColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        HorizontalDivider(
            color = contentColor.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
        )
        BottomNavigation(
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = {
                        Icon(imageVector = icons[index], contentDescription = item)
                    },
                    label =
                        { if (selectedItem == index) {
                            Text(text = item) // Show label for the selected item
                        } else {
                            Text(text = "") // Provide an empty label for consistency
                        }
                    },
                    selected = selectedItem == index,
                    onClick = { onItemSelected(index) },
                    alwaysShowLabel = false // Prevent always showing labels for non-selected items
                )
            }
        }
    }
}