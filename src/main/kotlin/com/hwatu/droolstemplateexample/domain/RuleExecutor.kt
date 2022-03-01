package com.hwatu.droolstemplateexample.domain


import com.hwatu.droolstemplateexample.common.KLogger
import org.drools.core.ClassObjectFilter
import org.drools.template.DataProvider
import org.drools.template.DataProviderCompiler
import org.kie.api.KieBase
import org.kie.api.builder.Message
import org.kie.api.io.ResourceType
import org.kie.api.runtime.KieSessionsPool
import org.kie.internal.utils.KieHelper


class RuleExecutor(
    rules: List<Rule>
) {
    private val kieBase: KieBase
    private val kieSessionPool: KieSessionsPool

    companion object : KLogger {
        const val RULE_TEMPLATE_PATH = "/rules/rule-template.drt"
    }

    init {
        val dataProvider = RuleDataProvider(rules)
        val drl = DataProviderCompiler().compile(dataProvider, this.javaClass.getResourceAsStream(RULE_TEMPLATE_PATH))

        log.info("===drl===")
        log.info(drl)
        log.info("========")

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
        val kieSession = kieSessionPool.newKieSession()
        kieSession.setGlobal("logger", log)
        kieSession.insert(inputData)
        kieSession.fireAllRules()
        @Suppress("UNCHECKED_CAST")
        val results = (kieSession.getObjects(ClassObjectFilter(RuleResult::class.java)) as Collection<RuleResult>).toList()
        kieSession.dispose()
        return results
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
