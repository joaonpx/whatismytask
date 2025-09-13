package com.github.joaonpx.whatismytask

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.util.messages.MessageBusConnection
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import git4idea.repo.GitRepositoryManager

class BranchCommitMessageHandlerFactory : CheckinHandlerFactory() {

    private var lastBranchName: String? = null

    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        val connection: MessageBusConnection = panel.project.messageBus.connect()

        // Ouvinte de mudanças de repositório (inclui checkout de branch)
        connection.subscribe(GitRepository.GIT_REPO_CHANGE, GitRepositoryChangeListener { repo ->
            fillBranchName(panel)
        })

        return object : CheckinHandler() {
            override fun includedChangesChanged() {
                super.includedChangesChanged()
                fillBranchName(panel)
            }
        }
    }

    private fun fillBranchName(panel: CheckinProjectPanel) {
        val repository = GitRepositoryManager.getInstance(panel.project).repositories.firstOrNull()
        val branchName = repository?.currentBranch?.name ?: return

        // Pega apenas a parte após a última barra
        val shortBranchName = branchName.substringAfterLast("/")

        // Atualiza apenas se a branch realmente mudou
        if (shortBranchName == lastBranchName) return
        lastBranchName = shortBranchName

        // Garantindo que a atualização da UI ocorra na EDT
        ApplicationManager.getApplication().invokeLater {
            panel.commitMessage = "$shortBranchName: "
        }
    }
}