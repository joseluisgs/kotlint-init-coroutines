import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

    fun main() = runBlocking<Unit> {
        launch { // context of the parent, main runBlocking coroutine
            log("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            log("Unconfined: I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher 
            log("Default: I'm working in thread ${Thread.currentThread().name}")
        }
        launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
            log("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }

        launch(Dispatchers.IO) {
            log("IO: I'm working in thread ${Thread.currentThread().name}")
        }

        /*launch(Dispatchers.Main) { // En el hilo principal
            log("IO: I'm working in thread ${Thread.currentThread().name}")
        }*/
    }