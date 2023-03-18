package com.example.orbitmvisample.utils

/**
 * Multiple variables "let" for 2, 3, 4 and 5 variables
 * https://code.luasoftware.com/tutorials/kotlin/kotlin-check-multiple-variables-for-not-null/#solution-4
 */

// 2 variables
fun <T1 : Any, T2 : Any, R : Any> multiLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null)
        block(p1, p2) else null
}

// 3 variables
fun <T1 : Any, T2 : Any, T3 : Any, R : Any> multiLet(
    p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null)
        block(p1, p2, p3) else null
}

// 4 variables
fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> multiLet(
    p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null)
        block(p1, p2, p3, p4) else null
}

// 5 variables
fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> multiLet(
    p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null)
        block(p1, p2, p3, p4, p5) else null
}