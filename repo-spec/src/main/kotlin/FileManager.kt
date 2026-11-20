abstract class FileManager{

    protected fun compare(firstVersion : List<Int>,secondVersion : List<Int>) : Int{
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

    protected fun extractVersion(fileName : String) : List<Int>{
        return fileName.split("-").last()
            .split(".")
            .dropLast(1)
            .map { it.toInt() }
    }
}