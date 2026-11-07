
interface DependencyValidator<T> {
    fun checkDependencies(dependencies: Array<String>, repoPath: T) : Boolean
}