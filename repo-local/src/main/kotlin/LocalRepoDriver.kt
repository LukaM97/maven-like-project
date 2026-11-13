package local_repo

import RepoManager
import com.google.gson.Gson
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

    override fun upload(file: File, nameParts: List<String>, dependencies: Array<String>) {

        initializeRepository()

        if(!dv.checkDependencies(dependencies, repoPath!!)){
            print("Missing dependencies in the repository")
            return
        }

        val newFileName = "${nameParts[0]}-${nameParts[1]}-${nameParts[2]}.${file.extension}"

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
        val configFile = folderFiles.firstOrNull{it.name.endsWith(".json")}?:throw Exception("File not found")
        val dependencies = getDependencies(configFile)
        if(dependencies.isNotEmpty()){
            downloadDependencies(dependencies, downloadPath)
        }

        processAndCopyFile(downloadPath, targetFile)
    }

    fun getFullPath(nameParts: List<String>, repoPath: Path) : Path {
        return repoPath.resolve(nameParts[0]).resolve(nameParts[1]).resolve(nameParts[2])
    }

    fun processAndCopyFile(downloadPath: String, fileInRepo: File) {
        val dir = File(downloadPath)
        if(!dir.exists()) dir.mkdirs()

        val baseFileName = fileInRepo.name.substringBeforeLast("-")
        val dirFiles = dir.listFiles(){file -> file.isFile && file.name.startsWith(baseFileName) }
        if(dirFiles.isNullOrEmpty()){
            fileInRepo.copyTo(File(dir, fileInRepo.name), overwrite = false)
            println("Downloading ${fileInRepo.name} to $downloadPath ...")
            return
        }
        val fileInDir = dirFiles.first()

        val v1 = extractVersion(fileInRepo)
        val v2 = extractVersion(fileInDir)

        val comparedValue = compare(v1, v2)

        if(comparedValue > 0){
            fileInRepo.copyTo(File(downloadPath.plus("\\${fileInRepo.name}")), true)
            fileInDir.delete()
            println("Updated to newer version: ${fileInRepo.name}")
        }else {
            println("Skipping ${fileInRepo.name}; an equal or newer version already exists.")
        }

    }

    private fun compare(firstVersion : List<Int>,secondVersion : List<Int>) : Int{
        var i = 0
        var q = 0
        while(i < firstVersion.size && i < secondVersion.size) {
            if(firstVersion[i] != secondVersion[i]) {
                q = firstVersion[i].compareTo(secondVersion[i])
                break
            }
            i++
        }
        return q
    }

    private fun extractVersion(file : File) : List<Int> {
        return file.name.split("-").last()
                .split(".")
                .dropLast(1)
                .map { it.toInt() }
    }

    fun getDependencies(configFile : File) : List<String> {
        val gson : Gson = Gson()

        class FileConfig(
            val group: String,
            val project: String,
            val version: String,
            val dependencies: List<String>
        )

        val configObj = gson.fromJson(configFile.readText(),FileConfig::class.java)
        return configObj.dependencies
    }

    fun downloadDependencies(dependencies: List<String>, downloadPath: String) {
        if(dependencies.isNotEmpty()){
            dependencies.forEach {
                val folderNames = it.split("-").toMutableList()
                dv.prepareFolderNames(folderNames)

                download(downloadPath, folderNames)
            }
        }
    }
}