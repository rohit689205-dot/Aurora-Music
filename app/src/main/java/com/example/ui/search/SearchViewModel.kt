package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YTMusicRepository
import com.example.data.YTMusicSearchResult
import com.example.ui.state.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ARTISTS("Artists"),
    ALBUMS("Albums"),
    PLAYLISTS("Playlists")
}

class SearchViewModel(
    private val repository: YTMusicRepository = YTMusicRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<YTMusicSearchResult>>(UiState.Empty)
    val searchState: StateFlow<UiState<YTMusicSearchResult>> = _searchState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    init {
        setupDebouncedSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        _query
            .debounce(400L)
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isBlank()) {
                    _searchState.value = UiState.Empty
                } else {
                    executeSearch(q)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onFilterSelected(filter: SearchFilter) {
        _selectedFilter.value = filter
    }

    fun retry() {
        if (_query.value.isNotBlank()) {
            executeSearch(_query.value)
        }
    }

    private fun executeSearch(q: String) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading

            repository.search(query = q)
                .collect { result ->
                    result.fold(
                        onSuccess = { searchResult ->
                            if (searchResult.songs.isEmpty() &&
                                searchResult.artists.isEmpty() &&
                                searchResult.albums.isEmpty() &&
                                searchResult.playlists.isEmpty()
                            ) {
                                _searchState.value = UiState.Empty
                            } else {
                                _searchState.value = UiState.Success(searchResult)
                            }
                        },
                        onFailure = { error ->
                            _searchState.value = UiState.Error(error.localizedMessage ?: "Failed to load music search results")
                        }
                    )
                }
        }
    }
}
