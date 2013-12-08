package com.opentext.dropmerge

import org.codehaus.groovy.runtime.StackTraceUtils

class DebugUtil {
    public static String getLineNumber() {
        StackTraceElement ste = StackTraceUtils.sanitize(new Exception()).stackTrace[1]
        return "${ste.fileName}:${ste.lineNumber}"
    }
}
