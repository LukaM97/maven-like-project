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

    override fun upload(file: File, nameParts: Triple<String, String, String>, dependencies: Array<String>) {

        initializeRepository()

        if(!dv.checkDependencies(dependencies, repoPath!!)){
            print("Missing dependencies in the repository")
            return
        }

        val newFileName = "${nameParts.first}-${nameParts.second}-${nameParts.third}.${file.extension}"

        val fullPath = getFullPath(nameParts, repoPath!!)
        if(!fullPath.exists()){
            fullPath.createDirectories()
        }
        if(fullPath.resolve(newFileName).exists()){
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

    fun getFullPath(nameParts: Triple<String, String, String>, repoPath: Path) : Path {
        return repoPath.resolve(nameParts.first).resolve(nameParts.second).resolve(nameParts.third)
    }

    override fun download(downloadPath: String, folderNames: Triple<String, String, String>) {

        repoPath = rootPath.resolve("repo_")

        if(repoPath == null || !repoPath!!.exists()){
            throw Exception("Repository folder not found")
        }

        val fullPath = getFullPath(folderNames, repoPath!!)
        if(!fullPath.exists()){
            println("File does not exist")
            return
        }
        val targetFolder = File(fullPath.toString())
        val folderFiles = targetFolder.listFiles()
        val targetFile = folderFiles!![0]

        try{
            targetFile.copyTo(File(downloadPath.plus("\\${targetFile.name}")), false)
        }catch(e : Exception){
            println(e.message)
        }finally {
            println("Downloading ${targetFile.name} to $downloadPath ...")
        }
    }

}