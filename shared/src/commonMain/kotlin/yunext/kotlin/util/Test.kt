package yunext.kotlin.util

import kotlin.coroutines.RestrictsSuspension

suspend fun aaa(){

}
suspend fun main() {
    val now = 1L
    toDateStr {
        now.toDateStr()
        toDateStrU {
            now.toDateStrU()
        }
    }
//    now.toDateStr()

    val test2 :suspend MyScope2.()->Unit = {
        now.toDateStr4()
//        aaa()
    }
    test2(myScope2)
    val a1 = myScope2.run {
        aaa()

        now.toDateStr3()
    }
    val a2 = myScope2 {
//        aaa()
//        now.toDateStr4()
        now.toDateStr3()


    }

    buildList {
        add(1)
    }

    sequence<Int> {

    }





    now.toDateStr1()
    println(a1)
}

