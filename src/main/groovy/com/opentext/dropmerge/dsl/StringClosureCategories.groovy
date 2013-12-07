package com.opentext.dropmerge.dsl

class StringClosureCategories {
    static Closure<String> plus(Closure<String> closure, String string) {
        return { closure.call() + string }
    }

    static Closure<String> plus(Closure<String> closureA, Closure<String> closureB) {
        return { closureA.call() + closureB.call() }
    }
}
