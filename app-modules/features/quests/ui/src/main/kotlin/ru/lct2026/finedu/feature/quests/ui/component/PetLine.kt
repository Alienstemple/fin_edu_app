package ru.lct2026.finedu.feature.quests.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Дзынь с репликой: питомец слева, пузырь справа. */
@Composable
internal fun PetLine(look: PetLook, stage: PetStage, mood: PetMood, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PetView(look = look, mood = mood, stage = stage, modifier = Modifier.width(88.dp))
        SpeechBubble(text = text, modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun PetLinePreview() {
    FinEduPreview {
        PetLine(
            look = PetLook(PetFur.LILAC, PetHat.CAP),
            stage = PetStage.SPRY,
            mood = PetMood.SHOCK,
            text = "Ого. Это мне?"
        )
    }
}
