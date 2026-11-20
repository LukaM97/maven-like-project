package rs.raf.sk

import Model.FileConfig
import RepoManager
import java.util.Scanner
import java.util.ServiceLoader

fun main() {
    val scanner = Scanner(System.`in`)
    val filePath = getFilePath(scanner)

    val serviceLoader = ServiceLoader.load(RepoManager::class.java)
    val exporterServices = mutableMapOf<String,RepoManager> ()

    serviceLoader.forEach{
            service -> exporterServices.put(service.type.toString(), service)
    }

    val localDriver = exporterServices.get(RepoType.LOCAL.toString())!!
    val googleDriveDriver = exporterServices.get(RepoType.DRIVE.toString())!!

    val missingDepConfigs = listOf(
        FileConfig().apply {
            group = "ls"
            project = "proj1"
            version = "1.2.0"
        },
        FileConfig().apply {
            group = "rs.raf.sk"
            project = "asad"
            version = "1.0.0"
            dependencies = arrayOf("ls-ideademos-1.0.0.zip", "ls-proj1-1.2.0.zip")
        }
    )

    val allDepConfigs = listOf(
        FileConfig().apply {
            group = "ls"
            project = "proj1"
            version = "1.2.0"
        },
        FileConfig().apply {
            group = "ls"
            project = "ideademos"
            version = "1.0.0"
        },
        FileConfig().apply {
            group = "rs.raf.sk"
            project = "asad"
            version = "1.0.0"
            dependencies = arrayOf("ls-ideademos-1.0.0.zip", "ls-proj1-1.2.0.zip")
        }
    )

    val mode = promptScenario(scanner)

    when (mode) {
        "missing" -> {
            println("=== SCENARIO: Missing dependency ===")
            runUploadBatch(localDriver, googleDriveDriver, filePath, missingDepConfigs)
        }
        "valid" -> {
            println("=== SCENARIO: All dependencies present ===")
            runUploadBatch(localDriver, googleDriveDriver, filePath, allDepConfigs)
        }
        else -> println("Unknown scenario '$mode'. Use 'missing' or 'valid'.")
    }
}

private fun runUploadBatch(
    localDriver: RepoManager,
    driveDriver: RepoManager,
    filePath: String,
    configs: List<FileConfig>
) {
    configs.forEach { config ->
        val nameParts = listOf(config.group, config.project, config.version)
        localDriver.upload(filePath, nameParts, config.dependencies)
        driveDriver.upload(filePath, nameParts, config.dependencies)
    }
}

private fun promptScenario(scanner: Scanner): String {
    print("Choose scenario (missing / valid): ")
    return scanner.nextLine().trim()
}

fun getFilePath(scanner : Scanner): String{
    var filePath : String = ""

    do {
        println("Enter path to file:")
        filePath = scanner.nextLine()
        if(filePath.isEmpty()){
            println("path can not be empty")
        }
    }while(filePath == "")

    return filePath
}