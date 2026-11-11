interface RepoUploader {
    fun upload(filePath: String, nameParts: Triple<String, String, String>, dependencies: Array<String>)
}