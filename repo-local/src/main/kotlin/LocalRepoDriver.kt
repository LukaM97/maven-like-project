package local_repo

import RepoManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class LocalRepoDriver() : RepoManager() {
    override val type = RepoType.LOCAL
    private val dv = LocalDependencyValidator()
    private val fm = LocalFileManager()
    val rootPath: Path = Paths.get(System.getProperty("user.home"), "Desktop")
    var repoPath : Path? = null

    override fun initializeRepository() {
        val repoDir = rootPath.resolve("repo_").toFile()
        if (!repoDir.exists()) {
            repoPath = Files.createDirectory(repoDir.toPath())
            println("----------------------------------")
            println("root folder created at $rootPath")
            println("----------------------------------")

            rcm.createLocalRootConfig(repoPath!!)
        } else {
            repoPath = repoDir.toPath()
        }
    }

    override fun upload(file: File, nameParts: List<String>, dependencies: Array<String>) {

        initializeRepository()

        if(!dv.checkDependencies(dependencies, repoPath!!)){
            print("Missing dependencies in the repository")
            return
        }

        val newFileName = "${nameParts[0]}-${nameParts[1]}-${nameParts[2]}"

        val fullPath = getFullPath(nameParts, repoPath!!)
        if(!fullPath.exists()){
            fullPath.createDirectories()
        }
        if(fullPath.resolve(newFileName.plus(".${file.extension}")).exists()){
            println("File already exists")
            return
        }

        try{
            file.copyTo(File(fullPath.resolve(newFileName).toString()), false)
            rcm.generateLocalFileConfig(nameParts, fullPath, dependencies)

        }catch(e : Exception){
            println(e.message)
        }finally {
            println("Uploading ${file.name} to $fullPath ...")
        }
    }

    override fun download(downloadPath: String, folderNames: List<String>) {

        repoPath = rootPath.resolve("repo_")

        if(repoPath == null || !repoPath!!.exists()){
            throw Exception("Repository folder not found")
        }

        val pathToRepoLib = getFullPath(folderNames, repoPath!!)
        if(!pathToRepoLib.exists()){
            println("File does not exist")
            return
        }
        val folderFiles = File(pathToRepoLib.toString()).listFiles()!!
        val targetFile = folderFiles.firstOrNull{!it.name.endsWith(".json")}?:throw Exception("File not found")
        val configFile = folderFiles.firstOrNull{it.name.endsWith(".json")}?:throw Exception("Config file not found")
        val dependencyFolders = dv.getPreparedDependencyFolders(configFile)
        if(dependencyFolders.isNotEmpty()){
            dependencyFolders.forEach {
                folderNames -> download(downloadPath, folderNames)
            }
        }

        fm.processAndCopyFile(downloadPath, targetFile)
    }

    fun getFullPath(nameParts: List<String>, repoPath: Path) : Path {
        return repoPath.resolve(nameParts[0]).resolve(nameParts[1]).resolve(nameParts[2])
    }
}