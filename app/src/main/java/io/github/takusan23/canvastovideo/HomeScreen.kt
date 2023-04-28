package io.github.takusan23.canvastovideo

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigate: (MainScreenPath) -> Unit) {
    val navList = remember {
        listOf(
            MainScreenPath.BasicScreen,
            MainScreenPath.SlideShowScreen,
            MainScreenPath.EndRollScreen,
        )
    }

    LazyColumn {
        items(navList) { path ->
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                onClick = { onNavigate(path) }
            ) { Text(text = path.name) }
        }
    }
}