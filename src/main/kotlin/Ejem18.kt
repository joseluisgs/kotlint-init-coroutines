import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

/*Crearemos una clase productora de planetas y una clase consumidora de planetas.
La única función de la clase productora será transmitir los elementos de la lista de planetas a través de un canal.
La función de la clase consumidora será recibir los elementos enviados por la clase productora y filtrarlos
según el número de lunas para luego transformarlos y obtener como resultado solamente los nombres de los planetas.

Según la documentación oficial, los datos transmitidos a través del canal pueden ser consumidos de manera continua
de dos maneras: con la función consumeEach y con un ciclo for valiéndose del hecho de que un canal implementa iteradores.
Cuando se consumen los datos aplicando el patrón Fan-Out, si uno de los consumidores falla, podría repercutir en todos los
involucrados, es decir, tanto en los emisores como en los demás receptores. A continuación vamos a desarrollar un ejemplo
comparativo simple.

*/

// Renombro para que no se lie con otros ficheros
data class Planet2(val name: String, val volume: Long, val radius: Double, val moons: Int, val rings: Boolean)

// Creamos nuestro propio Scope, que hereda de CoroutineScope
class PlanetsProducer2: CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private val planets = listOf(
        Planet("Mercury", 60_827_208_742, 2_439.7, 0, false),
        Planet("Venus", 928_415_345_893, 6_051.8, 0, false),
        Planet("Earth", 1_083_206_916_846, 6_371.00, 1, false),
        Planet("Mars", 163_115_609_799, 3_389.5, 2, false),
        Planet("Jupiter", 1_431_281_810_739_360, 69_911.0, 79, true),
        Planet("Saturn", 827_129_915_150_897, 58_232.0, 83, true),
        Planet("Uranus", 68_334_355_695_584, 25_362.0, 27, true),
        Planet("Neptune", 62_525_703_987_421, 24_622.0, 14, true)
    )

    // Vamos la diferencia con el Ejem16
    // Nos ahorramos el Stream
    fun getChannel(): ReceiveChannel<Planet> = produce {
        planets.forEach { send(it) }
    }

    fun release() {
        this.job.cancel()
    }

}

class PlanetsConsumer2: CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    suspend fun processPlanetsStream(planets: ReceiveChannel<Planet>) = withContext(Dispatchers.Default) {
        planets.filterByMoons(2)
            .mapToName()
            .consumeEach { planet ->
                log("Planet consumed: $planet")
            }
    }

    private fun ReceiveChannel<Planet>.filterByMoons(moons: Int): ReceiveChannel<Planet> = produce {
        consumeEach { planet ->
            if (planet.moons >= moons)
                send(planet)
        }
    }

    private fun ReceiveChannel<Planet>.mapToName(): ReceiveChannel<String> = produce {
        consumeEach { planet -> send(planet.name) }
    }

    fun release() {
        this.job.cancel()
    }

}

fun main() {
    log("Start")

    val planetsProducer = PlanetsProducer2()
    val planetsConsumer = PlanetsConsumer2()

    runBlocking {
        launch {
            planetsConsumer.processPlanetsStream(planetsProducer.getChannel())
            planetsConsumer.release()
            planetsProducer.release()
        }
    }

    log("End")
}