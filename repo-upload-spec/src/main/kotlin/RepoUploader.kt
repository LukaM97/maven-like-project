interface RepoUploader {
    fun upload(filePath: String, nameParts: List<String>, dependencies: Array<String>)
}