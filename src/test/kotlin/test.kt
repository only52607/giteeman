import com.ooooonly.giteeman.GiteeFile
import java.io.File
import java.net.URL

@ExperimentalStdlibApi
fun main(){
    //GiteeFile("ooooonly","lua-mirai-project","ScriptCenter").forEachChildFiles {
        //println(it.fileName)
        //println(it.dataString)
    //}

    //println("demos/复读机.lua".replace(Regex("/.*?$"), ""))

    val f = GiteeFile("ooooonly","lua-mirai","demos/复读机.lua")
    //println(f.dataString)
    f.saveToFile("""C:\Users\86182\Desktop\复读机.lua""")
}