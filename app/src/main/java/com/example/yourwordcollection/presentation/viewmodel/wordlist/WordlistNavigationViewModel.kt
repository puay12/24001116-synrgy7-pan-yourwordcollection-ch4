package com.example.yourwordcollection.presentation.viewmodel.wordlist

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.savedstate.SavedStateRegistryOwner
import com.example.yourwordcollection.data.datasource.local.WordLocalDataSourceImpl
import com.example.yourwordcollection.data.datasource.local.database.room.AppDatabase
import com.example.yourwordcollection.data.model.Word
import com.example.yourwordcollection.data.repository.WordlistRepositoryImpl
import com.example.yourwordcollection.domain.repository.WordlistRepository
import kotlinx.coroutines.launch


class WordlistNavigationViewModel(
    private val wordlistRepository: WordlistRepository
) : ViewModel() {
    companion object {
        fun provideFactory(
            owner: SavedStateRegistryOwner,
            context: Context,
        ) =
            object : AbstractSavedStateViewModelFactory(owner, null) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle,
                ): T {
                    val roomDatabase = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "favorite_word_list"
                    ).build()
                    val wordlistRepository: WordlistRepository = WordlistRepositoryImpl(
                        wordLocalDataSource = WordLocalDataSourceImpl(
                            wordlistDao = roomDatabase.wordlistDao()
                        )
                    )
                    return WordlistNavigationViewModel(
                        wordlistRepository = wordlistRepository
                    ) as T
                }
            }
    }

    private val _liveWordlistData: MutableLiveData<List<Word>> = MutableLiveData()
    private val _error: MutableLiveData<Throwable> = MutableLiveData<Throwable>()
    private val _favoriteWord: MutableLiveData<Word?> = MutableLiveData()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getWordList(arguments: Bundle) : LiveData<List<Word>>? {
        loadData(arguments)
        return _liveWordlistData
    }

    fun getError() : LiveData<Throwable> {
        return _error
    }

    fun getFavoriteWord() : LiveData<Word?> {
        return _favoriteWord
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun loadData(arguments: Bundle) {
        _liveWordlistData.value = arguments.getParcelableArray("wordList", Word::class.java)?.toList()
    }

    fun saveToFavorite(
        id: Int? = null,
        name: String,
        imgUrl: String
    ) {
        viewModelScope.launch {
            val word = Word(
                id = id,
                name = name,
                imgUrl = imgUrl
            )

            wordlistRepository.saveWordToFavorite(word)
        }
    }

    fun deleteFromFavorite(word: Word) {
        viewModelScope.launch {
            wordlistRepository.deleteWordFromFavorite(word)
        }
    }

    fun loadWordByName(name: String){
        viewModelScope.launch {
            try{
                _favoriteWord.value = wordlistRepository.fetchWordByName(name)
            } catch (error: Throwable) {
                _error.value = error
            }
        }
    }
}