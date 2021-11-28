import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/*
Vamos a crear un programa que tendrá un objeto que hace operaciones a partir de valores aleatorios.
Es decir, tendremos un objeto que realiza 3 operaciones independientes.
Cada una de las operaciones va a generar 3 números aleatorios y los va a sumar para terminar haciendo su tarea correspondiente
usando el resultado de la suma. Las tres operaciones independientes son las siguientes:
Sumar 3 números aleatorios y verificar que el resultado es un número par.
Sumar 3 números aleatorios y calcular el valor promedio del resultado.
Sumar 3 números aleatorios y calcular el valor elevado al cuadrado del resultado.
También vamos a simular la producción de errores con sus respectivos lanzamientos de excepciones.
Será necesario entonces declarar 2 funciones para obtener un número aleatorio. Una de ellas obtendrá el número aleatorio
correctamente y la otra función simulará la producción de un error lanzando una excepción. Éstas dos funciones son la siguientes:

 */

class Operator: CoroutineScope {

    // Estáticos de la clase
    companion object {
        const val OPERATION_IS_EVEN = "Operation (Is Even)"
        const val OPERATION_AVERAGE = "Operation (Average)"
        const val OPERATION_SQUARE = "Operation (Square)"
    }

    /*
    Dado que las 3 operaciones son independientes, en caso de que se genere un error en alguna, las otras dos operaciones
    deben continuar sin ningún problema. Para ello utilizaremos un SupervisorJob
     */
    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        log("Exception Handler Caught: ( $exception ) with suppressed ${exception.suppressed.contentToString()}")
    }
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default + exceptionHandler


    // Suma tres números y devuleve si es par
    fun sumIsEvenAsync() = async {
        val sum = sumThreeRandomNumbers(OPERATION_IS_EVEN)
        log("$OPERATION_IS_EVEN: Sum = $sum")
        sum % 2 == 0
    }

    // Suma tres numeros y devuleve la media
    fun sumAverageAsync() = async {
        val sum = sumThreeRandomNumbers(OPERATION_AVERAGE)
        log("$OPERATION_AVERAGE: Sum = $sum")
        sum.toFloat() / 3
    }

    // Suma tres numeros y devuleve el cuadrado
    fun sumSquareAsync() = async {
        val sum = sumThreeRandomNumbers(OPERATION_SQUARE)
        log("$OPERATION_SQUARE: Sum = $sum")
        sum * sum
    }

    // Suma tres números aleatorios
    private suspend fun sumThreeRandomNumbers(operation: String): Int = coroutineScope {
       // val r1 = async { myRandomNumberFail(operation) } // Simulamos que falla
        val r1 = async { myRandomNumberSuccess(operation) }
        val r2 = async { myRandomNumberSuccess(operation) }
        val r3 = async { myRandomNumberSuccess(operation) }

        r1.await() + r2.await() + r3.await()
    }

    // Obtiene un número aleatorio
    private suspend fun myRandomNumberSuccess(operation: String): Int = withContext(Dispatchers.Default) {
        log("$operation Retrieving random number...")
        delay(2000) //Simulating a heavy computation
        val random = (1..100).random()
        log("$operation Random Number = $random")
        random
    }

    // Cuando falla
    private suspend fun myRandomNumberFail(operation: String): Int = withContext(Dispatchers.Default) {
        log("$operation Retrieving random number...")
        delay(1000) //Simulating a heavy computation
        throw IllegalStateException("$operation Retrieving random number failed.")
    }

    fun release() {
        this.job.cancel()
    }
}





fun main() {
    log("Start")

    val operator = Operator()

    runBlocking {
        val defIsEven = operator.sumIsEvenAsync()
        val defAverage = operator.sumAverageAsync()
        val defSquare = operator.sumSquareAsync()

        /*
        Creamos una coroutine con el constructor launch para consumir el dato cuando esté listo. Usamos un bloque try-catch
        para consumir el dato usando la función await para evitar que el programa se cuelgue en caso de que se genere un error,
         */
        launch {
            try {
                log("${Operator.OPERATION_IS_EVEN} Result = ${defIsEven.await()}")
            } catch (e: Exception) {
                log("${Operator.OPERATION_IS_EVEN} Caught: ( $e )")
            } finally {
                log("--- NOTHING CONTAINING \"${Operator.OPERATION_IS_EVEN}\" SHOULD APPEAR AFTER THIS LINE ---")
                delay(3000)
            }
        }

        launch {
            try {
                log("${Operator.OPERATION_AVERAGE} Result = ${defAverage.await()}")
            } catch (e: Exception) {
                log("${Operator.OPERATION_AVERAGE} Caught: ( $e )")
            } finally {
                log("--- NOTHING CONTAINING \"${Operator.OPERATION_AVERAGE}\" SHOULD APPEAR AFTER THIS LINE ---")
                delay(3000)
            }
        }

        launch {
            try {
                log("${Operator.OPERATION_SQUARE} Result = ${defSquare.await()}")
            } catch (e: Exception) {
                log("${Operator.OPERATION_SQUARE} Caught: ( $e )")
            } finally {
                log("--- NOTHING CONTAINING \"${Operator.OPERATION_SQUARE}\" SHOULD APPEAR AFTER THIS LINE ---")
                delay(3000)
            }
        }
    }

    operator.release()

    log("End")
}

