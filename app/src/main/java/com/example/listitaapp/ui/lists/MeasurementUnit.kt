package com.example.listitaapp.ui.lists

import androidx.annotation.StringRes
import com.example.listitaapp.R

enum class MeasurementUnit(val id: String, @StringRes val labelRes: Int) {
    Units("units", R.string.unit_units),
    Kilograms("kg", R.string.unit_kilograms),
    Grams("g", R.string.unit_grams),
    Liters("l", R.string.unit_liters),
    Milliliters("ml", R.string.unit_milliliters),
    Pieces("pcs", R.string.unit_pieces);

    companion object {
        fun fromId(id: String): MeasurementUnit? = values().firstOrNull { it.id == id }
    }
}
