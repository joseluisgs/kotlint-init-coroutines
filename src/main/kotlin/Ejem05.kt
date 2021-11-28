import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

// Dispachers
    fun main() = runBlocking<Unit> {
        launch { // context of the parent, main runBlocking coroutine
            log("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            log("Unconfined: I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher todo lo que de el procesador
            log("Default: I'm working in thread ${Thread.currentThread().name}")
        }
        launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
            log("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }

        launch(Dispatchers.IO) { // Ira en el de IO, 64 hilos
            log("IO: I'm working in thread ${Thread.currentThread().name}")
        }

        /*launch(Dispatchers.Main) { // En el hilo principal y solo para Android igual que lifeCycle
            log("IO: I'm working in thread ${Thread.currentThread().name}")
        }*/
    }