package com.hwatu.droolstemplateexample.domain

import com.hwatu.droolstemplateexample.common.KLogger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RuleExecutorTest {
    companion object : KLogger

    @Test
    fun droolsOneRuleTest() {
        val ruleOne = Rule(
            ruleName = "1",
            condition = "a>1"
        )

        val rules = listOf(ruleOne)
        val executor = RuleExecutor(rules)
        val inputData = mapOf<String, Any>("a" to 3)
        val result = executor.execute(inputData)
        log.info("result: ${result.joinToString()}")
        val actual = result.map { it.ruleName }.containsAll(listOf("1"))
        assertTrue(actual)
    }

    @Test
    fun droolsMultipleRuleTest() {
        val ruleOne = Rule(
            ruleName = "1",
            condition = "a>1"
        )

        val ruleTwo = Rule(
            ruleName = "2",
            condition = "b>2"
        )

        val ruleThree = Rule(
            ruleName = "3",
            condition = "c>3"
        )

        val ruleFour = Rule(
            ruleName = "4",
            condition = "c>2&&a>1"
        )

        val ruleFive = Rule(
            ruleName = "5",
            condition = "c>2||a>4"
        )

        val ruleSix = Rule(
            ruleName = "6",
            condition = "c>5||a>4"
        )

        val rules = listOf(ruleOne, ruleTwo, ruleThree, ruleFour, ruleFive, ruleSix)
        val executor = RuleExecutor(rules)
        val inputData = mapOf(
            "a" to "3",
            "b" to "1",
            "c" to "4"
        )
        val result = executor.execute(inputData)
        log.info("result: ${result.joinToString()}")
        val actual = result.map { it.ruleName }.containsAll(listOf("1", "3", "4", "5"))
        assertTrue(actual)
    }

    @Test
    fun droolsPreconditionTest() {
        val ruleOne = Rule(
            ruleName = "1",
            condition = "a>1"
        )

        val ruleTwo = Rule(
            ruleName = "2",
            preRuleCondition = "1",
            condition = "d==\"abc\"&&c>3"
        )

        val ruleThree = Rule(
            ruleName = "3",
            preRuleCondition = "!2",
            condition = "b>2"
        )

        val rules = listOf(ruleOne, ruleTwo, ruleThree)
        val executor = RuleExecutor(rules)
        val inputData = mapOf(
            "a" to "2",
            "b" to "3",
            "c" to "4",
            "d" to "abc"
        )
        val result = executor.execute(inputData)
        log.info("result: ${result.joinToString()}")
        val actual = result.map { it.ruleName }
        assertTrue(actual.containsAll(listOf("1", "2")))
        assertTrue(actual.size == 2)
    }

    @Test
    fun droolsPreconditionTest2() {
        val ruleOne = Rule(
            ruleName = "1",
            condition = "a>1"
        )

        val ruleTwo = Rule(
            ruleName = "2",
            preRuleCondition = "!1",
            condition = "d==\"abc\"&&c>3"
        )

        val ruleThree = Rule(
            ruleName = "3",
            preRuleCondition = "!2",
            condition = "b>2"
        )

        val rules = listOf(ruleOne, ruleTwo, ruleThree)
        val executor = RuleExecutor(rules)
        val inputData = mapOf(
            "a" to "2",
            "b" to "3",
            "c" to "4",
            "d" to "abc"
        )
        val result = executor.execute(inputData)
        log.info("result: ${result.joinToString()}")
        val actual = result.map { it.ruleName }
        assertTrue(actual.containsAll(listOf("1", "3")))
        assertTrue(actual.size == 2)
    }
}
