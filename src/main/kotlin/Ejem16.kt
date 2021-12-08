import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.CoroutineContext
/*
Crearemos una clase productora de planetas y una clase consumidora de planetas.
La única función de la clase productora será transmitir los elementos de la lista de planetas a través de un canal.
La función de la clase consumidora será recibir los elementos enviados por la clase productora y filtrarlos
según el número de lunas para luego transformarlos y obtener como resultado solamente los nombres de los planetas.
 */

// Pojo de Planeta
data class Planet(val name: String, val volume: Long, val radius: Double, val moons: Int, val rings: Boolean)

class PlanetsProducer {

    private val channel = Channel<Planet>() // Canal no limitdo, es decir, no tiene un bvuffer de tamaño fijo

    // Lista de planetas
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

    // Función que devuelve el canal, lo devolvemos de solo lectura. Para recibir
    fun getChannel(): ReceiveChannel<Planet> = this.channel

    // Función que devuelve la lista de planetas a través del canal
    // Una vez terminemos, cerramos el canal
    suspend fun processPlanetsStream() = withContext(Dispatchers.Default) {
        planets.forEach { channel.send(it) }
        channel.close()
    }

}

// Vamos a crear un propio contexto de ejecución, para que no se ejecute en el thread principal
class PlanetsConsumer: CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    // Función que recibe el canal y lo procesa con la logica de filterByMoon
    suspend fun processPlanetsStream(planets: ReceiveChannel<Planet>) = withContext(Dispatchers.Default) {
        planets.filterByMoons(2)
            .mapToName()
                // realiza una acción por cada elemento, es el forEach para canales, se puede usar un for
            .consumeEach { planet ->
                log("Planet consumed: $planet")
            }
    }

    // Función que recibe los planetas y los filtra según el número de lunas
    // Devuelve una lista de planetas filtrados en el canal donde se transmitirán los planetas que cumplen la condición del filtro
    private fun ReceiveChannel<Planet>.filterByMoons(moons: Int): ReceiveChannel<Planet> {
        val filteredElementsChannel = Channel<Planet>()
        launch {
            consumeEach { planet ->
                if (planet.moons >= moons)
                    filteredElementsChannel.send(planet)
            }
            filteredElementsChannel.close()
        }
        return filteredElementsChannel
    }

    // Función que recibe los planetas y solo pasa el nombre
    private fun ReceiveChannel<Planet>.mapToName(): ReceiveChannel<String> {
        val mappedElementsChannel = Channel<String>()
        launch {
            consumeEach { planet -> mappedElementsChannel.send(planet.name) }
            mappedElementsChannel.close()
        }
        return mappedElementsChannel
    }

    // Libera los trabajos
    fun release() {
        this.job.cancel()
    }

}

fun main() {
    log("Start")
    // Creamos el productor y el consumidor
    val planetsProducer = PlanetsProducer()
    val planetsConsumer = PlanetsConsumer()

    runBlocking {
        // Lanzamos a producir planetas sobre el stream
        launch {
            planetsProducer.processPlanetsStream()
        }

        launch {
            // Lanzamos a consumir los planetas a traves del canal del productor en modo solo lectura
            planetsConsumer.processPlanetsStream(planetsProducer.getChannel())
            planetsConsumer.release()
        }
    }

    log("End")
}