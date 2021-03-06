package pl.karolmichalski.githubrepos.presentation.screens.repos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import pl.karolmichalski.githubrepos.data.exceptions.BlankInputException
import pl.karolmichalski.githubrepos.data.models.Repo
import pl.karolmichalski.githubrepos.domain.interactors.RepoListUseCase
import pl.karolmichalski.githubrepos.presentation.App
import javax.inject.Inject

class ReposViewModel(app: App) : AndroidViewModel(app) {

	class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			@Suppress("UNCHECKED_CAST")
			return ReposViewModel(application as App) as T
		}
	}

	val keywords = MutableLiveData<String>()
	val repoList = MutableLiveData<List<Repo>>().apply { value = ArrayList() }
	val isLoading = MutableLiveData<Boolean>().apply { value = false }
	val errorMessage = MutableLiveData<String>()

	@Inject
	lateinit var repoListUseCase: RepoListUseCase

	init {
		app.appComponent.inject(this)
	}

	fun findRepos() {
		repoListUseCase.execute(keywords.value)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe { isLoading.postValue(true) }
				.doFinally { isLoading.postValue(false) }
				.subscribeBy(
						onSuccess = { repoList.value = it },
						onError = {
							if (it is BlankInputException)
								repoList.value = ArrayList()
							errorMessage.value = it.localizedMessage
						})
	}

}