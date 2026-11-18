package local_repo

import DependencyValidator
import com.google.gson.Gson
import java.io.File
import java.nio.file.Path

internal class LocalDependencyValidator : DependencyValidator<Path> {

    override fun checkDependencies(dependencies: Array<String>, repoPath: Path) : Boolean {
        var exists = false
        if(dependencies.isEmpty()) {
            exists = true
        }
        dependencies.forEach { dependency ->
            val nameParts = dependency.split("-").toMutableList()
            prepareFolderNames(nameParts)

            exists = checkNestedFolders(repoPath.toFile(), nameParts, dependency )
            if(!exists) return false
        }

        return exists
    }

    private fun checkNestedFolders(currentPath: File, parts: MutableList<String>, fileName : String, index: Int = 0) : Boolean {
        if(index > parts.size - 1) {
            val targetFile = File(currentPath, fileName)
            return targetFile.exists() && targetFile.isFile
        }

        val nextFolder = currentPath.resolve(parts[index])

        return nextFolder.exists() && checkNestedFolders(nextFolder, parts, fileName, index + 1)
    }

    private fun prepareFolderNames(nameParts: MutableList<String>){
        val lastNamePart = nameParts.last().split(".").toMutableList()
        lastNamePart.removeLast()
        val lastFolderName = lastNamePart.joinToString(separator = ".")
        nameParts.removeLast()
        nameParts.addLast(lastFolderName)
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

    fun getPreparedDependencyFolders(configFile : File) : List<List<String>> {
        val dependencies = getDependencies(configFile)
        return dependencies.map { dep ->
            val folderNames = dep.split("-").toMutableList()
            prepareFolderNames(folderNames)
            folderNames
        }
    }
}