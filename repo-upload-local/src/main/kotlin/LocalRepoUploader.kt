package local_repo

import RepoUploader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LocalRepoUploader() : RepoUploader() {
    override val type = RepoType.LOCAL
    val rootPath: Path = Paths.get(System.getProperty("user.home"), "Desktop")
    var repoPath : Path? = null

    override fun initializeRepository() : Path {
        val repoDir = rootPath.resolve("repo_").toFile()
        if(!repoDir.exists()) {
            repoPath = Files.createDirectory(repoDir.toPath())
            println("----------------------------------")
            println("root folder created at $rootPath")
            println("----------------------------------")

            createRootConfig(repoPath!!)
        }else{
            repoPath = repoDir.toPath()
        }

        return repoPath!!
    }

}