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
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        val connection: MessageBusConnection = panel.project.messageBus.connect()

        connection.subscribe(GitRepository.GIT_REPO_CHANGE, GitRepositoryChangeListener {
            fillBranchName(panel)
        })

        return object : CheckinHandler() {}
    }

    private fun fillBranchName(panel: CheckinProjectPanel) {
        val repository = GitRepositoryManager.getInstance(panel.project).repositories.firstOrNull()
        val branchName = repository?.currentBranch?.name ?: return

        val shortBranchName = branchName.substringAfterLast("/")

        ApplicationManager.getApplication().invokeLater {
            panel.commitMessage = "$shortBranchName: "
        }
    }
}