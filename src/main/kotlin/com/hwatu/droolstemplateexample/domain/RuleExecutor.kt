package com.hwatu.droolstemplateexample.domain


import com.hwatu.droolstemplateexample.common.KLogger
import org.drools.core.ClassObjectFilter
import org.drools.template.DataProvider
import org.drools.template.DataProviderCompiler
import org.kie.api.KieBase
import org.kie.api.builder.Message
import org.kie.api.io.ResourceType
import org.kie.api.runtime.KieSessionsPool
import org.kie.internal.command.CommandFactory
import org.kie.internal.utils.KieHelper


class RuleExecutor(
    rules: List<Rule>
) {
    private val kieBase: KieBase
    private val kieSessionPool: KieSessionsPool

    companion object : KLogger {
        const val RULE_TEMPLATE_PATH = "/rules/rule-template.drt"
        const val RULE_COMMAND_RESULT_NAME = "results"
    }

    init {
        val dataProvider = RuleDataProvider(rules)
        val drl = DataProviderCompiler().compile(dataProvider, this.javaClass.getResourceAsStream(RULE_TEMPLATE_PATH))

        log.debug("===drl===")
        log.debug(drl)
        log.debug("========")

        val kieHelper = KieHelper().addContent(drl, ResourceType.DRL)
        val results = kieHelper.verify()
        if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
            val messages: List<Message> = results.getMessages(Message.Level.WARNING, Message.Level.ERROR)
            for (message in messages) {
                log.error("Error: " + message.text)
            }
            throw IllegalStateException("Compilation errors were found. Check the logs.")
        }
        kieBase = kieHelper.build()
        kieSessionPool = kieBase.newKieSessionsPool(10)
    }

    fun execute(inputData: Map<String, Any>): List<RuleResult> {
        val statelessKieSession = kieSessionPool.newStatelessKieSession()
        val kieCommands = listOf(
            CommandFactory.newSetGlobal("logger", log),
            CommandFactory.newInsert(inputData),
            CommandFactory.newFireAllRules(),
            CommandFactory.newGetObjects(ClassObjectFilter(RuleResult::class.java), RULE_COMMAND_RESULT_NAME)
        )
        val executionResult = statelessKieSession.execute(CommandFactory.newBatchExecution(kieCommands))

        @Suppress("UNCHECKED_CAST")
        return (executionResult.getValue(RULE_COMMAND_RESULT_NAME) as Collection<RuleResult>).toList()
    }


    class RuleDataProvider(rules: List<Rule>) : DataProvider {
        val iterator = rules.iterator()
        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        override fun next(): Array<String?> {
            val next = iterator.next()
            val preRuleCondition = next.preRuleCondition?.let { parsePreconditionRule(next.preRuleCondition) }
            return arrayOf(next.ruleName, preRuleCondition, "Map(${next.condition})")
        }

        private fun parsePreconditionRule(preRuleCondition: String): String {
            return if (preRuleCondition.startsWith("!")) {
                "not RuleResult(ruleName==${preRuleCondition.substring(1)})"
            } else {
                "RuleResult(ruleName==${preRuleCondition.substring(0)})"
            }

        }
    }
}
