import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/*
Si conoces sobre patrones de diseño, muy probablemente conoces el patrón creacional Singleton dada su simpleza.
Un objeto Singleton es aquel que se instancia una sola vez y vive durante el tiempo de vida de la aplicación.
Sin embargo, existe algo de controversia porque este patrón es considerado por muchos desarrolladores como un antipatrón.
Yo por mi parte considero que, según el uso que se le da, puede ser un patrón o un antipatrón.
En este artículo vas a conocer el objeto GlobalScope, su relación con el patrón de diseño Singleton,
y por qué debes evitar crear coroutines con él.

Crear una coroutine valiéndose del objeto GlobalScope, se asemeja a crear una coroutine con un Job no cancelable
como los que vimos en un artículo anterior. Es decir, la coroutine romperá la relación con el Scope donde fue creada
y vivirá hasta que la aplicación finalice su ejecución. Por esta razón su uso se desaconseja a tal punto que solo
debe hacerse cuando sabes muy bien lo que estás haciendo.

 */

fun main() = runBlocking {
    log("Start")

    val stepTime: Long = 2500
    val obj = FileReader()

    log("Reading files")
    obj.readFileWithLocalScope("Users.txt")
    obj.readFileWithGlobalScope("Customers.txt")

    log("Waiting for $stepTime milliseconds...")
    delay(stepTime)

    log("Reading another file")
    obj.readFileWithGlobalScope("Services.txt")

    log("Waiting for $stepTime milliseconds...")
    delay(stepTime)

    log("Releasing resources...")
    obj.release()
    log("Resources have been released!")

    log("Waiting for $stepTime milliseconds before closing...")
    delay(stepTime)

    log("End")
}

class FileReader : CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun readFileWithLocalScope(filename: String) = launch {
        for(i in 1..Int.MAX_VALUE) {
            delay(500)
            log("Reading file \"$filename\" with local scope: Line #$i")
        }
    }

    fun readFileWithGlobalScope(filename: String) = GlobalScope.launch {
        for(i in 1..Int.MAX_VALUE) {
            delay(500)
            log("Reading file \"$filename\" with global scope: Line #$i")
        }
    }

    fun release() {
        this.job.cancel()
    }

}

/*
Lo primero que notarás es que aparentemente no hay diferencia entre la ejecución de la coroutine del Scope de la clase y
la coroutine del Scope global del programa. Cuando creas una coroutine con el objeto GlobalScope, por defecto usará
el Dispatchers.Default para ejecutar el bloque de código. Además observa que al hacer otra llamada a la función readFileWithGlobalScope,
se crea otra coroutine, a diferencia de lo que pasaría con un objeto Singleton, que se obtendría siempre la misma instancia.
 Ésto es precisamente lo peligroso de crear coroutines usando el objeto GlobalScope, se crearán nuevas coroutines todo el tiempo.
Finalmente, como puedes ver en el resultado, aún cancelando el Job de la clase, las dos coroutines que simulan la lectura
de archivos usando el GlobalScope se mantienen en ejecución.
Si has trabajado en el entorno Android, sabrás que cuando abres una pantalla se crea una instancia de un Activity.
Cuando volteas el dispositivo, vale decir, cuando hay un cambio de configuración, el Activity se desecha para darle
 lugar a una instancia nueva del mismo Activity. Quiero que imagines lo que pasaría si dentro del método onCreate del
 Activity creas una coroutine usando el objeto GlobalScope que hace peticiones a un servidor y volteas el dispositivo
 una y otra vez. Estarás creando coroutines nuevas globales que liberarán los recursos hasta que cierres la aplicación.
 Eventualmente tendrás muchas coroutines en ejecución a la vez. Todo ésto omitiendo el riesgo potencial que tienes de
botar el programa si dentro de la coroutine se modifica la interfaz de usuario de una instancia de un Activity que ya no existe.
Aunque el ejemplo es un poco absurdo dado lo mal implementado que estaría el programa si hacemos eso, lo que quiero dejar
muy claro es que el desconocimiento de algunos detalles acerca del funcionamiento de ciertos frameworks junto al uso
indiscriminado y alegre de algunas herramientas solo por el hecho de parecer simples e inofensivas, pueden llevarte a
crear el programa más ineficiente de tu vida.
Para finalizar debo decir que espero que este artículo te haya quedado absolutamente claro. La idea de postergarlo
 hasta esta instancia fue para evitar que adquirieras malos hábitos muy pronto en el aprendizaje de coroutines.
 Considero que en este punto ya conoces lo mínimo necesario para implementar las coroutines en tus proyectos de manera
 efectiva, así que ya podemos entrar de lleno en un tema que tenemos pendiente y aunque no es difícil, es un poco más
 complicado. Me refiero a la creación de coroutines con el constructor produce y el uso de canales o Channels.

 */