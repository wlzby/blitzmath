package com.mawelly.blitzmath.ui.components

import com.mawelly.blitzmath.core.LocalPlatformServices

import blitzmath.app.generated.resources.Res
import blitzmath.app.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

object ScientistResources {
    fun getPortrait(id: String): DrawableResource? {
        return when (id) {
            "einstein_portrait" -> Res.drawable.einstein_portrait
            "newton_portrait" -> Res.drawable.newton_portrait
            "tesla_portrait" -> Res.drawable.tesla_portrait
            "turing_portrait" -> Res.drawable.turing_portrait
            "curie_portrait" -> Res.drawable.curie_portrait
            "gauss_portrait" -> Res.drawable.gauss_portrait
            "pythagoras_portrait" -> Res.drawable.pythagoras_portrait
            "cahit_arf_portrait" -> Res.drawable.cahit_arf_portrait
            "ataturk_portrait" -> Res.drawable.ataturk_portrait
            else -> null
        }
    }
}
