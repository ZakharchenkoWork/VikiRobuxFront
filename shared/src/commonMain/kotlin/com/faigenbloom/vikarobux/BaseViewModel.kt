package com.faigenbloom.vikarobux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

open class BaseViewModel {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(Dispatchers.IO + job)

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = {
            block()
        })
    }
}