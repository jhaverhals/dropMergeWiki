package com.opentext.dropmerge.dsl

abstract class Spec {
    abstract Map<String, Closure<String>> getInputData()
}
