package com.hwatu.droolstemplateexample.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface KLogger {
    val log: Logger get() = LoggerFactory.getLogger(this.javaClass)
}
