package ru.lct2026.finedu.feature.learn.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class GlossaryViewModel @Inject constructor(private val contentRepository: ContentRepository) :
    StatefulViewModel<GlossaryUiState>(GlossaryUiState.Loading) {

    init {
        viewModelScope.launch {
            setState(GlossaryUiState.Content(contentRepository.content().glossary))
        }
    }
}
