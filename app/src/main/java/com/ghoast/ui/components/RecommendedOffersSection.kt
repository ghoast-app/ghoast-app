package com.ghoast.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghoast.model.Offer
import com.ghoast.viewmodel.RecommendationViewModel
import androidx.compose.runtime.getValue

@Composable
fun RecommendedOffersSection(
    viewModel: RecommendationViewModel,
    onOfferClick: (Offer) -> Unit
) {
    val offers by viewModel.recommendedOffers.collectAsState()

    if (offers.isNotEmpty()) {
        Column {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(offers) { offer ->
                    RecommendedOfferCard(
                        offer = offer,
                        onClick = { onOfferClick(offer) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // 👈 πολύ σημαντικό
        }
    }
}
