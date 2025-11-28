package com.example.app_verduras.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.app_verduras.R

@Composable
fun AppFooter() {
    val footerComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.footer_app_movil))
    LottieAnimation(
        composition = footerComposition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp) // Aumentamos la altura a 150.dp
    )
}
