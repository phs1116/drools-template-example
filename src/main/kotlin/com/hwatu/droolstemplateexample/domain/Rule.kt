package com.hwatu.droolstemplateexample.domain

data class Rule(
    val ruleName: String,
    val preRuleCondition: String? = null,
    val condition: String
)
