[![official JetBrains project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

## Showing progress
Dispatchers.Default에서 작업을 하고 withContext(Dispatchers.Main)으로 작업마다 전달
```kotlin
suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = ...

    var allUsers = emptyList<User>()
    for ((index, repo) in repos.withIndex()) {
        val users = service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        allUsers = (allUsers + users).aggregate()
        updateResults(allUsers, index == repos.lastIndex)
    }
}

// used
launch(Dispatchers.Default) {
    loadContributorsProgress(service, req) { users, completed ->
        withContext(Dispatchers.Main) {
            updateResults(users, startTime, completed)
        }
    }
}
```

## NotCancellable
 GlobalScope는 coroutineScope의 자식이 아니라 cancel을 회피
```kotlin
val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
    GlobalScope.async {
        log("starting loading for ${repo.name}")
        delay(3000)
        service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }
}

// cancel 버튼을 누르면 deferreds 취소, but GlobalScope는 cancel 되지 않음
deferreds.awaitAll().flatten().aggregate().setUpCancellation()

```


## CONCURRENT
async는 Deferred 반환, Deferred는 await를 명령하면 실행
```kotlin
launch {
    val users = loadContributorsConcurrent(service, req)
    updateResults(users, startTime)
}

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        async {
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    deferreds.awaitAll().flatten().aggregate()
}
```


## SUSPEND
suspend 함수는 기본쓰레드를 차단하지 않는 함수로 coroutine으로 호출 해야함
```kotlin
launch {
    val users = loadContributorsSuspend(service, req)
    updateResults(users, startTime)
}

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    return repos.flatMap { repo ->
        service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}
```

# Introduction to Coroutines and Channels Hands-On Lab

This repository is the code corresponding to the
[Introduction to Coroutines and Channels](https://play.kotlinlang.org/hands-on/Introduction%20to%20Coroutines%20and%20Channels/01_Introduction)
Hands-On Lab. 
