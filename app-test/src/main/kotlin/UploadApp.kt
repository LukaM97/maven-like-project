package rs.raf.sk

import RepoManager
import java.util.Scanner
import java.util.ServiceLoader
import kotlin.emptyArray

fun main() {
    val scanner = Scanner(System.`in`)

    val filePath = getFilePath(scanner)
    val nameParts = setFileName(scanner)
    //val dependencies = arrayOf("ls-ideademos-1.0.0.zip","ls-proj1-1.2.0.zip")
    //val dependencies = arrayOf("as-projekat1-2.0.0.zip")

    val serviceLoader = ServiceLoader.load(RepoManager::class.java)
    val exporterServices = mutableMapOf<String,RepoManager> ()

    serviceLoader.forEach{
            service -> exporterServices.put(service.type.toString(), service)
    }

    exporterServices.get(RepoType.LOCAL.toString())!!.upload(filePath,nameParts, emptyArray())
    //exporterServices.get(RepoType.LOCAL.toString())!!.upload(filePath,nameParts, dependencies)
    //exporterServices.get(RepoType.DRIVE.toString())!!.upload(filePath,nameParts, emptyArray<String>())
}

fun getFilePath(scanner : Scanner): String{
    var filePath : String = ""

    do {
        println("Enter path to file:")
        filePath = scanner.nextLine()
        if(filePath == ""){
            println("path can not be null")
        }
    }while(filePath == "")

    return filePath
}

fun setFileName(scanner: Scanner) : List<String> {
    var groupName : String? = null
    var projectName : String? = null
    var version : String? = null

    println("groupName: ")
    groupName = scanner.nextLine()
    println("projectName: ")
    projectName = scanner.nextLine()
    println("version: ")
    version = scanner.nextLine()

    return listOf(groupName,projectName,version)
}