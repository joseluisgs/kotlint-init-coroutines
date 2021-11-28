import kotlinx.coroutines.*

// CourotinesNames

fun main() {
    coroutineName()
}

fun coroutineName() {
    log("Start")

    runBlocking (CoroutineName("My Parent Coroutine")) {

        log("RunBlocking start ( ${this.coroutineContext[CoroutineName]} )")

        launch (CoroutineName("My Coroutine A")) {
            repeat(5) {
                delay(1000)
                log("Hello from ( ${this.coroutineContext[CoroutineName]} ) : $it")
            }
        }

        launch (Dispatchers.Default + CoroutineName("My Coroutine B")) {
            repeat(10) {
                delay(700)
                log("Hello from ( ${this.coroutineContext[CoroutineName]} ) : $it")
            }
        }

        log("RunBlocking end ( ${this.coroutineContext[CoroutineName]} )")
    }

    log("End")
}