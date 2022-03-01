# Drools Rule Template Example
## Rule
- Drools Rule Engine에 적용 할 Rule을 정의
- condition에 적용할 값과 조건을 작성한다.
- preRuleCondition은 이전에 어떤 룰을 만족해야 실행 가능한지를 정의할 수 있다.

## RuleResult
- 각 Rule을 실행한 결과

## RuleExecutor
- 정의한 Rule들을 기반으로 Drools Rule을 생성한다.
- 또한 입력받은 데이터를 생성한 Rule 기반으로 Rule Engine을 실행하고 결과를 반환한다.
