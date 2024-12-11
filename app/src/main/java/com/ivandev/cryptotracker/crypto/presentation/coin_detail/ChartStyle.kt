package com.ivandev.cryptotracker.crypto.presentation.coin_detail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

data class ChartStyle(
    val chartLineColor: Color,
    val unselectedColor: Color,
    val selectedColor: Color,
    val helperLinesThicknessPx: Float,
    val axisLineThicknessPx: Float,
    val labelFontSize: TextUnit,
    val minYLabelSpacingDp: Dp,
    val verticalPaddingDp: Dp,
    val horizontalPaddingDp: Dp,
    val xAxisLabelSpacing: Dp
)
