# Kotlin Init Coroutines
Jugando con Corrutinas en Kotlin

[![Kotlin](https://img.shields.io/badge/Code-Kotlin-blueviolet)](https://kotlinlang.org/)
[![LISENCE](https://img.shields.io/badge/Lisence-MIT-green)]()
![GitHub](https://img.shields.io/github/last-commit/joseluisgs/kotlint-init-coroutines)


![imagen](https://miro.medium.com/max/2000/1*I3OMp4jIytzR7GKpRkEiAA.png)

## Acerca De
Distintos ejemplos de uso de Corrutinas y Concurrencia en Kotlin

## Suspender vs Bloquear. Funciones de suspensión
En el contexto de concurrencia, bloquear un hilo significa que el hilo se mantendrá fuera de uso mientras éste encuentre 
algo que lo bloquee. Ésto podría ser la espera de la liberación de un recurso, una llamada a la función Thread.sleep, 
una llamada a un servicio externo, etc. Mientras se encuentre en ese estado, el hilo no podrá ser usado para realizar otras tareas. 
Por el contrario, suspender un hilo significa que el hilo estará libre y listo para ser usado en la ejecución de otras tareas mientras 
se encuentra a la espera de la liberación de un recurso, una llamada a la función delay, una llamada a un servicio externo, etc.
La naturaleza de las coroutines es suspender la ejecución evitando a toda costa llamadas a funciones que bloquean. 
Ésta es la magia que le permite a las coroutines ser tan eficientes y de bajo consumo de recursos en comparación con los 
hilos regulares de siempre.

Las funciones de suspensión tienen la capacidad de suspender la ejecución de la corrutina mientras están haciendo su trabajo. 
Una vez que termina, el resultado de la operación se devuelve y se puede utilizar en la siguiente línea.

## Contexto de corrutina
Contexto de corrutina
El contexto de corrutina es un conjunto de reglas y configuraciones que definen cómo se ejecutará la corrutina. 
Por debajo, es una especie de Map, con un conjunto de claves y valores posibles.

Una de las posibles configuraciones es el dispatcher que se utiliza para identificar el hilo donde se ejecutará la corrutina. 
Este dispatcher se puede proporcionar de dos maneras:
- Explícitamente: Configuramos manualmente el dispatcher que se utilizará.
- Por el scope de la corrutina

## Constructores de Corrutinas
Existen varios constructores de coroutines, cada uno para un caso específico. Estos constructores son: runBlocking, launch, async y produce. 
También es posible crear coroutines dentro de otra coroutine sin ninguna limitación. Por lo tanto, una coroutine puede tener muchas coroutines “hijas”, 
y éstas a su vez pueden tener más coroutines “hijas” y así infinitamente. Ejem02

### runBlocking
Éste es un caso especial de constructor. Crea una coroutine y suspende el hilo que lo ejecuta hasta que la coroutine finalice, es decir, bloquea el hilo actual hasta que se terminen todas las tareas dentro de esa corrutina.
Este constructor no debe ser usado nunca, excepto para hacer pruebas unitarias de nuestras suspend functions. 
También es posible usarlo en el método main para jugar con las coroutines tal y como lo estamos haciendo en este momento. 
NUNCA utilices este constructor de coroutines en código de producción. 
Debido a que runBlocking no es una función de extensión de la interfaz CoroutineScope, se puede usar en el interior de cualquier función.

Lo que en realidad pasa al crear una coroutine con runBlocking es que el hilo que la crea esperará a que la coroutine finalice 
para continuar con la ejecución en la línea que está inmediatamente después. 

### launch
Este constructor crea una coroutine devolviendo un objeto de tipo Job. Debido a que este constructor es una función de 
extensión de la interfaz CoroutineScope, se puede llamar solamente desde adentro de una coroutine o dentro de una suspend function. 
Se utiliza para hacer tareas que no requieren la devolución de ningún valor.

Como mencioné anteriormente, el constructor launch se puede llamar solamente desde adentro de una coroutine o dentro de una suspend function. 

#### job.join
Con está función, puedes bloquear la corrutina asociada con el job hasta que todos los jobs hijos hayan finalizado.
#### job.cancel
Esta función cancelará todos sus jobs hijos asociados.

### async
Este constructor crea una coroutine devolviendo un objeto de tipo Deferred<T> siendo T el tipo de dato esperado.
Por ejemplo Int, String, etc. Debido a que el constructor async es una función de extensión de la interfaz CoroutineScope, 
se puede llamar solamente desde adentro de una coroutine o dentro de una suspend function. Se utiliza para hacer tareas que 
requieren la devolución de algún valor. O por ejemplo poder lanzar dos cosas en paralelo.
Permite ejecutar varias tareas en segundo plano en paralelo. No es una función de suspensión en sí misma, 
por lo que cuando ejecutamos async, el proceso en segundo plano se inicia, pero la siguiente línea se ejecuta de inmediato
Este objeto tiene una nueva función  llamada await() que es la que bloquea. Llamaremos a await() solo cuando necesitemos el resultado. 
Si el resultado aún no esta listo, la corrutina se suspende en ese punto. Si ya tenemos el resultado, simplemente lo devolverá y continuará. De esta manera, puedes ejecutar tantas tareas en segundo plano como necesites.

### produce
Este constructor crea una coroutine que se utiliza para la comunicación por medio de canales (Channels) con otras coroutines


## Dispatchers
Los dispatchers son un tipo de contextos de corrutina que especifican el hilo o hilos que pueden ser utilizados por la corrutina 
para ejecutar su código. Hay dispatchers que solo usan un hilo (como Main) y otros que definen un grupo de hilos que se optimizarán 
para ejecutar todas las corrutinas que reciben.

Si recuerdas, al principio dijimos que 1 hilo puede ejecutar muchas corrutinas, por lo que el sistema no creará 1 hilo por corrutina, 
sino que intentará reutilizar los que ya están vivos.

Tenemos cuatro dispatchers principales:

- Default: Se usará cuando no se defina un dispatcher, pero también podemos configurarlo explícitamente. 
Este dispatcher se utiliza para ejecutar tareas que hacen un uso intensivo de la CPU, principalmente cálculos de la propia App, algoritmos, etc. 
Puede usar tantos subprocesos como cores tenga la CPU. Ya que estas son tareas intensivas, no tiene sentido tener más ejecuciones al mismo tiempo, porque la CPU estará ocupada.
- IO: Utiliza este para ejecutar operaciones de entrada/salida. En general, todas las tareas que bloquearán el hilo mientras esperan la respuesta de otro sistema: 
peticiones al servidor, acceso a la base de datos, sitema de archivos, sensores… ya que no usan la CPU, se puede tener muchas en ejecución al mismo tiempo, p
por lo que el tamaño de este grupo de hilos es de 64. Las Apps lo que más hacen, es interactuar con el dispositivo y hacer peticiones de red, por lo que probablemente usarás este la mayoría del tiempo.
- Unconfined: Si no te importa mucho qué hilo se utiliza, puedes usar este dispatcher. Es difícil predecir qué hilo se usará, así que no lo uses a menos que estés muy seguro de lo que estás haciendo.
- Main: Este es un dispatcher especial que se incluye en las librerías de corrutinas relacionadas con interfaz de usuario. En particular, en Android, utilizará el hilo de UI.

## Secuencialidad vs Paralelismo
El orden de ejecución de cada sentencia en una coroutine es secuencial. 
Si queremos lanzar en hilos distintos y ademas coordinar la asincronia o paralelimiadad debemos usar async. Posteriomente,
sincronizaremos con await. Ejem03

## CourentineScope
Cada vez que usamos un constructor de coroutines en realidad estamos haciendo una llamada a una función que recibe 
como primer parámetro un objeto de tipo CoroutineContext. 
Los constructores launch y async son en realidad funciones de extensión de la interfaz CoroutineScope

### Global scope
Es un scope general que se puede usar para cualquier corrutina que deba continuar con la ejecución mientras la aplicación 
se está ejecutando. Por lo tanto, no deben estar atados a ningún componente específico que pueda ser destruido.
Crear una coroutine valiéndose del objeto GlobalScope, se asemeja a crear una coroutine con un Job no cancelable. Es decir, la coroutine romperá la relación con el Scope donde fue creada
y vivirá hasta que la aplicación finalice su ejecución. Por esta razón su uso se desaconseja a tal punto que solo
debe hacerse cuando sabes muy bien lo que estás haciendo.

## Extra – Convertir callbacks a corrutinas
Existe una función llamada suspendCancellableCoroutine, que nos permite pasar de un mundo a otro:
```kotlin
suspend fun suspendAsyncLogin(username: String, password: String): User =
    suspendCancellableCoroutine { continuation ->
        userService.doLoginAsync(username, password) { user ->
            continuation.resume(user)
        }
    }
```

Esta función devuelve un objeto continuation que se puede utilizar para devolver el resultado del callback. 
Simplemente llame a continuation.resume y ese resultado será devuelto por la suspending function a la corrutina padre. ¡Es así fácil!

## Autor

Codificado con :sparkling_heart: por [José Luis González Sánchez](https://twitter.com/joseluisgonsan)

[![Twitter](https://img.shields.io/twitter/follow/joseluisgonsan?style=social)](https://twitter.com/joseluisgonsan)
[![GitHub](https://img.shields.io/github/followers/joseluisgs?style=social)](https://github.com/joseluisgs)

### Contacto
<p>
  Cualquier cosa que necesites házmelo saber por si puedo ayudarte 💬.
</p>
<p>
    <a href="https://twitter.com/joseluisgonsan" target="_blank">
        <img src="https://i.imgur.com/U4Uiaef.png" 
    height="30">
    </a> &nbsp;&nbsp;
    <a href="https://github.com/joseluisgs" target="_blank">
        <img src="https://cdn.iconscout.com/icon/free/png-256/github-153-675523.png" 
    height="30">
    </a> &nbsp;&nbsp;
    <a href="https://www.linkedin.com/in/joseluisgonsan" target="_blank">
        <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/LinkedIn_logo_initials.png/768px-LinkedIn_logo_initials.png" 
    height="30">
    </a>  &nbsp;&nbsp;
    <a href="https://joseluisgs.github.io/" target="_blank">
        <img src="https://joseluisgs.github.io/favicon.png" 
    height="30">
    </a>
</p>


## Licencia
Este proyecto está licenciado bajo licencia **MIT**, si desea saber más, visite el fichero [LICENSE](./LICENSE) para su uso docente y educativo.

### Referencias
- [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines.html)
- https://medium.com/kotlin-en-android/coroutines-con-kotlin-constructores-de-coroutines-8a9e10c8187e
- https://devexperto.com/corrutinas/