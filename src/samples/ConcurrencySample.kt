package samples

import kotlinx.coroutines.*

//fun main() = runBlocking {
//    val deferred: Deferred<Int> = async(Dispatchers.Default) {
//        loadData()
//    }
//    log("waiting...")
//    log(deferred.await())
//}
//
//suspend fun loadData(): Int {
//    log("loading...")
//    delay(1000L)
//    log("loaded!")
//    return 42
//}


fun main() = runBlocking {
    val deferreds: List<Deferred<Int>> = (1..3).map {
        async {
            delay(1000L * it)
            println("Loading $it")
            it
        }
    }
    val sum = deferreds.awaitAll().sum()
    println("$sum")
}