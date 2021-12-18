import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Jugando con Job
// https://medium.com/kotlin-en-android/coroutines-con-kotlin-job-860db9237b86

fun main() {
    //isActive()
    //isCancelled()
    //isCompleted()
    fatherJob()
}

fun isActive() {
    log("Start")

    runBlocking {
        val job = launch {
            repeat(3) {
                log("Launch rep #$it : I'm active")
                delay(1000)
            }
            log("Launch : I'm finishing.")
        }

        delay(100)
        while (job.isActive) {
            log("RunBlocking : Job is active")
            delay(1000)
        }

        log("RunBlocking : Job is not active")
    }

    log("End")
}

fun isCancelled() {
    log("Start")

    runBlocking {
        val job = launch {
            repeat(10) {
                log("Launch rep #$it : I'm active")
                delay(1000)
            }
            log("Launch : I'm finishing.")
        }

        delay(2500)
        while (job.isActive) {
            log("RunBlocking : Job is active")
            delay(1000)
            log("RunBlocking : Cancelling Job")
            job.cancel()
        }

        if (job.isCancelled) {
            log("RunBlocking : Job is cancelled")
        } else {
            log("RunBlocking : Job is not cancelled")
        }
    }

    log("End")
}

fun isCompleted() {
    log("Start")

    runBlocking {
        val job = launch {
            repeat(3) {
                log("Launch rep #$it : I'm active")
                delay(1000)
            }
            log("Launch : I'm finishing.")
        }

        delay(100)
        while (job.isActive) {
            log("RunBlocking : Job is active")
            delay(1000)
        }

        if (job.isCancelled) {
            log("RunBlocking : Job is cancelled")
        }

        if (job.isCompleted) {
            log("RunBlocking : Job is completed")
        }
    }

    log("End")
}

fun fatherJob() {
    log("Start")

    runBlocking {
        var childJob: Job? = null
        val job = launch {

            childJob = launch {
                repeat(10) {
                    log("Child rep #$it : I'm active")
                    delay(1000)
                }

                log("Child : I'm finishing.")
            }

            delay(100)
            while (childJob?.isActive == true) {
                log("Launch : Job is active")
                delay(1000)
            }

            log("Launch : I'm finishing.")
        }

        delay(2500)
        while (job.isActive) {
            log("RunBlocking : Job is active")
            delay(1000)
            log("RunBlocking : Cancelling Job")
            job.cancel()
        }

        if (childJob?.isCancelled == true) {
            log("RunBlocking : Child Job is cancelled")
        }

        if (childJob?.isCompleted == true) {
            log("RunBlocking : Child Job is completed")
        }

        if (job.isCancelled) {
            log("RunBlocking : Job is cancelled")
        }

        if (job.isCompleted) {
            log("RunBlocking : Job is completed")
        }
    }

    log("End")
}