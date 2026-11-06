import java.io.File
import java.nio.file.Path

internal class DependencyValidator {

    fun checkDependencies(dependencies: Array<String>, repoPath: Path) : Boolean {
        var exists = false
        if(dependencies.isEmpty()) {
            exists = true
        }
        dependencies.forEach { dependency ->
            val nameParts = dependency.split("-").toMutableList()
            val extension = getExtension(nameParts)
            prepareFolderNames(nameParts)

            exists = checkNestedFolders(repoPath.toFile(), nameParts, dependency, extension )
            if(!exists) return false
        }

        return exists
    }

    fun checkNestedFolders(currentPath: File, parts: MutableList<String>, fileName : String, extension : String, index: Int = 0) : Boolean {
        if(index > parts.size - 1) {
            val targetFile = File(currentPath, fileName)
            return targetFile.exists() && targetFile.isFile
        }

        val nextFolder = currentPath.resolve(parts[index])

        return nextFolder.exists() && checkNestedFolders(nextFolder, parts, fileName, extension, index + 1)
    }

    fun prepareFolderNames(nameParts: MutableList<String>){
        val lastNamePart = nameParts.last().split(".").toMutableList()
        lastNamePart.removeLast()
        val lastFolderName = lastNamePart.joinToString(separator = ".")
        nameParts.removeLast()
        nameParts.addLast(lastFolderName)
    }

    fun getExtension(nameParts: List<String>) : String {
        return nameParts.last().split('.').last()
    }
}