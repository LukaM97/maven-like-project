abstract class DependencyValidator<T> {
    abstract fun checkDependencies(dependencies: Array<String>, repoPath: T) : Boolean

    fun prepareFolderNames(nameParts: MutableList<String>){
        val lastNamePart = nameParts.last().split(".").toMutableList()
        lastNamePart.removeLast()
        val lastFolderName = lastNamePart.joinToString(separator = ".")
        nameParts.removeLast()
        nameParts.addLast(lastFolderName)
    }
}