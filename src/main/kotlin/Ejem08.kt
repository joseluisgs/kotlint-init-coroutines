import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// Creando un Scope propio
class MyScope : CoroutineScope {

    private val job = Job() // Debemos tener un Job

    // Manejo de Excepciones
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        log("Exception Handler Caught: ( $exception ) with suppressed ${exception.suppressed.contentToString()}")
    }

    // Le decimos el contexto
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default + exceptionHandler

    // Podemos ponerle nombres a las corrutinas y a los métodos
    fun myFirstTask() = launch(CoroutineName("My Coroutine A")) {

        log("Job A start : Name: [ ${this.coroutineContext[CoroutineName]} ]")

        val jobA1 = launch(CoroutineName("My Coroutine A1")) {
            repeat(3) {
                delay(1000)
                log("Job A1: Name: ( ${this.coroutineContext[CoroutineName]} ) : $it")
            }
        }

        val jobA2 = async(CoroutineName("My Coroutine A2")) {
            repeat(3) {
                delay(700)
                log("Job A2: Name: ( ${this.coroutineContext[CoroutineName]} ) : $it")
            }
            (1..100).random()
        }

        joinAll(jobA1, jobA2)
        log("Job A end : Name: ( ${this.coroutineContext[CoroutineName]} )")
    }

    // Otro método
    fun mySecondTask() = launch(CoroutineName("My Coroutine B")) {
        log("Job B start : Name: ( ${this.coroutineContext[CoroutineName]} )")

        val jobB1 = launch(CoroutineName("My Coroutine B1")) {
            repeat(7) {
                delay(300)
                log("Job B1: Name: ( ${this.coroutineContext[CoroutineName]} ) : $it")
            }
        }

        jobB1.join()
        log("Job B end : Name: ( ${this.coroutineContext[CoroutineName]} )")
    }

    fun release() {
        this.job.cancel()
    }

}

fun main() {
    log("Start")

    val myObject = MyScope()

    runBlocking {
        log("RunBlocking start")

        val jobA = myObject.myFirstTask()
        val jobB = myObject.mySecondTask()

        joinAll(jobA, jobB)

        log("RunBlocking end")
    }

    myObject.release()

    log("End")
}