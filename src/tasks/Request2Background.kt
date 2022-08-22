package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User

suspend fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    updateResults(loadContributorsBlocking(service, req))
}