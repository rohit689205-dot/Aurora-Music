package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YouTubeRepository
import com.example.data.YouTubeSearchResult
import com.example.ui.state.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String) {
    ALL("All"),
    MUSIC("Music Videos"),
    PLAYLISTS("Playlists"),
    ARTISTS("Artists/Channels")
}

class YouTubeSearchViewModel(
    private val repository: YouTubeRepository = YouTubeRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<YouTubeSearchResult>>(UiState.Empty)
    val searchState: StateFlow<UiState<YouTubeSearchResult>> = _searchState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentNextPageToken: String? = null

    init {
        setupDebouncedSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        _query
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isBlank()) {
                    _searchState.value = UiState.Empty
                    currentNextPageToken = null
                } else {
                    executeSearch(q, _selectedFilter.value, pageToken = null)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onFilterSelected(filter: SearchFilter) {
        _selectedFilter.value = filter
        if (_query.value.isNotBlank()) {
            executeSearch(_query.value, filter, pageToken = null)
        }
    }

    fun retry() {
        if (_query.value.isNotBlank()) {
            executeSearch(_query.value, _selectedFilter.value, pageToken = null)
        }
    }

    private fun executeSearch(q: String, filter: SearchFilter, pageToken: String?) {
        viewModelScope.launch {
            if (pageToken == null) {
                _searchState.value = UiState.Loading
            } else {
                _isLoadingMore.value = true
            }

            val musicOnly = filter == SearchFilter.MUSIC
            repository.search(query = q, musicCategoryOnly = musicOnly, pageToken = pageToken)
                .collect { result ->
                    result.fold(
                        onSuccess = { searchResult ->
                            currentNextPageToken = searchResult.nextPageToken
                            if (pageToken != null && _searchState.value is UiState.Success) {
                                val currentData = (_searchState.value as UiState.Success).data
                                val combinedSongs = currentData.songs + searchResult.songs
                                val combinedPlaylists = currentData.playlists + searchResult.playlists
                                val combinedArtists = currentData.artists + searchResult.artists
                                _searchState.value = UiState.Success(
                                    YouTubeSearchResult(
                                        songs = combinedSongs,
                                        playlists = combinedPlaylists,
                                        artists = combinedArtists,
                                        nextPageToken = searchResult.nextPageToken,
                                        totalResults = searchResult.totalResults
                                    )
                                )
                            } else {
                                if (searchResult.songs.isEmpty() && searchResult.playlists.isEmpty() && searchResult.artists.isEmpty()) {
                                    _searchState.value = UiState.Empty
                                } else {
                                    _searchState.value = UiState.Success(searchResult)
                                }
                            }
                            _isLoadingMore.value = false
                        },
                        onFailure = { error ->
                            if (pageToken == null) {
                                _searchState.value = UiState.Error(error.localizedMessage ?: "Failed to load YouTube search results")
                            }
                            _isLoadingMore.value = false
                        }
                    )
                }
        }
    }

    fun loadNextPage() {
        val nextToken = currentNextPageToken
        if (!nextToken.isNullOrBlank() && !_isLoadingMore.value && _query.value.isNotBlank()) {
            executeSearch(_query.value, _selectedFilter.value, pageToken = nextToken)
        }
    }
}
