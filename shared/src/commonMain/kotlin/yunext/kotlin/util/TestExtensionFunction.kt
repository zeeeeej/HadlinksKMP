package yunext.kotlin.util

import yunext.kotlin.util.MyScope3.toDateStr
import kotlin.coroutines.RestrictsSuspension

fun Long.toDateStr1(): String {
    TODO()
}

object Utils {
    fun toDateStr2(timestamp: Long): String {
        TODO()
    }
}

@RestrictsSuspension
class MyScope2 internal constructor() {

    fun Long.toDateStr3(): String {
        return "toDateStr3:$this"
    }

    suspend fun Long.toDateStr4(): String {
        return "toDateStr3:$this"
    }

}

val myScope2: MyScope2
    by lazy { MyScope2() }

 inline  operator fun <T:Any>MyScope2.invoke(block:MyScope2.()->T):T {
    return this.block()
}

@DslMarker
annotation class MyScopeMaker

@MyScopeMaker
interface MyScope {
    fun Long.toDateStr(): String
}

private object MyScope3 : MyScope {
    override fun Long.toDateStr(): String {
        TODO("Not yet implemented")
    }

}

//@DslMarker
//annotation class UScopeMaker
@MyScopeMaker
interface UScope {
    fun Long.toDateStrU(): String
}

private object UScope3 : UScope {
    override fun Long.toDateStrU(): String {
        TODO("Not yet implemented")
    }

}

inline fun toDateStr(block: MyScope.() -> String): String = MyScope3.block()
inline fun toDateStrU(block: UScope.() -> String): String = UScope3.block()


