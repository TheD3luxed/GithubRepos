package pl.karolmichalski.githubrepos.presentation.repos

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import pl.karolmichalski.githubrepos.data.ApiInterface
import pl.karolmichalski.githubrepos.data.models.Repo
import pl.karolmichalski.githubrepos.presentation.App
import javax.inject.Inject

class ReposViewModel(app: App) : ViewModel() {

	class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			@Suppress("UNCHECKED_CAST")
			return ReposViewModel(application as App) as T
		}
	}

	init {
		app.appComponent.inject(this)
	}

	val keywords = MutableLiveData<String>()
	val repoList = MutableLiveData<List<Repo>>().apply { value = ArrayList() }
	val isLoading = MutableLiveData<Boolean>().apply { value = false }
	val errorMessage = MutableLiveData<String>()

	@Inject
	lateinit var apiInterface: ApiInterface

	fun findRepos() {
		keywords.value?.let { keywords ->
			apiInterface.findRepos(keywords)
					.doOnSubscribe { isLoading.postValue(true) }
					.doFinally { isLoading.postValue(false) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribeBy(
							onSuccess = { repoList.value = it.repoList },
							onError = { errorMessage.value = it.localizedMessage })
		}

	}

}