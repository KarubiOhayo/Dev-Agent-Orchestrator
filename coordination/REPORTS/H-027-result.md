# H-027 결과 보고서 (CLI `PARTIAL_SUCCESS` 소비 가드레일 보강)

## 상태
- 현재 상태: **완료 (구현 + 테스트 + 문서 반영 + 게이트 통과)**
- 실행일(KST): `2026-02-19`

## 변경 파일 목록
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliArguments.java`
- `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliArgumentsTest.java`
- `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `docs/cli-quickstart.md`
- `docs/code-agent-api.md`
- `coordination/REPORTS/H-027-result.md`
- `coordination/RELAYS/H-027-executor-to-review.md`

## 구현 요약
- CLI `generate/spec` 공통 옵션으로 `--fail-on-chain-failures=<true|false>`(기본 `false`)를 추가했습니다.
  - 허용 옵션/usage/help 반영
  - 옵션 파서 boolean 동치 비교 목록 반영
- 가드레일 종료코드를 `3`으로 고정했습니다.
  - `--fail-on-chain-failures=true` + `chainFailures[] > 0`일 때만 exit code `3`
  - 성공 출력(human/json)은 유지
- 출력 가시성을 보강했습니다.
  - human: `chainFailures > 0` 시 경고 1줄 출력
  - json: `data.hasChainFailures` 보조 필드 추가(기존 `data.summary`, `data.chainFailures[]` 유지)

## 신규 옵션 파싱/종료코드 계약 검증 결과
- `DevAgentCliRunnerTest.runGenerateReturnsExitCodeThreeWhenGuardrailEnabledAndChainFailuresExist`
  - `generate + PARTIAL_SUCCESS + chainFailures>0 + fail-on=true` -> exit code `3`
- `DevAgentCliRunnerTest.runSpecReturnsExitCodeThreeWhenGuardrailEnabledAndChainFailuresExist`
  - `spec + PARTIAL_SUCCESS + chainFailures>0 + fail-on=true` -> exit code `3`
- `DevAgentCliRunnerTest.runGenerateKeepsSuccessExitCodeWhenGuardrailDisabledByOption`
  - `fail-on=false` 명시 시 기존 성공 종료코드 `0` 유지
- `DevAgentCliRunnerTest.runGenerateKeepsSuccessExitCodeWhenNoChainFailuresEvenIfGuardrailEnabled`
  - `chainFailures=0 + fail-on=true` -> 종료코드 `0`
- `DevAgentCliArgumentsTest`에서 신규 옵션 boolean 파싱/동치(`true` vs `yes`)를 검증했습니다.

## `generate/spec` 경로별 가드레일 동작 검증 요약
- `generate` 경로:
  - true + failures>0 -> `3`
  - false/default -> `0`
  - true + failures=0 -> `0`
- `spec` 경로:
  - true + chained code failures>0 -> `3`
  - default(false) -> `0`

## 기본 동작(옵션 미사용) 하위호환 회귀 검증 결과
- 기존 `PARTIAL_SUCCESS` human/json 출력 검증 테스트는 그대로 통과했습니다.
- 기존 default 동작 검증(`runGenerateKeepsDefaultChainOptionsWhenNotProvided`, `runSpecKeepsDefaultCodeChainOptionsWhenNotProvided`)이 통과해 기존 체인 옵션 기본값/성공 종료코드 계약 유지가 확인됐습니다.
- JSON 계약은 기존 필드(`data.summary`, `data.chainFailures[]`)를 유지하면서 보조 필드만 추가했습니다.

## 문서 업데이트 요약
- `docs/cli-quickstart.md`
  - `--fail-on-chain-failures` 옵션 설명/예시 추가
  - `PARTIAL_SUCCESS` 소비 체크리스트(기본 모드 vs 가드레일 모드) 추가
  - JSON 예시에 `data.hasChainFailures` 반영
  - human 경고 출력 예시 추가
- `docs/code-agent-api.md`
  - `PARTIAL_SUCCESS` 소비 규약에 CLI 가드레일(`--fail-on-chain-failures=true`) 권고 추가

## 테스트 명령 및 결과
- 사전 검증:
  - 명령:
    - `./gradlew test --no-daemon --tests "me.karubidev.devagent.cli.DevAgentCliArgumentsTest" --tests "me.karubidev.devagent.cli.CliResultFormatterTest" --tests "me.karubidev.devagent.cli.DevAgentCliRunnerTest"`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 옵션은 opt-in이므로, 소비자가 `--fail-on-chain-failures`를 활성화하지 않으면 기존처럼 체인 실패가 있어도 종료코드는 `0`입니다.
- 외부 자동화가 stdout만 읽고 종료코드를 무시하면 가드레일 효과가 제한될 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `DevAgentCliRunner`에서 `generate/spec` 모두 `fail-on=true + chainFailures>0`일 때만 exit code `3`으로 일관되게 설정되는지
2. `CliResultFormatter`의 human 경고 1줄과 JSON `data.hasChainFailures` 추가가 기존 계약(`data.summary`, `data.chainFailures[]`)과 충돌하지 않는지
3. 옵션 미사용(default) 및 `false` 경로가 기존 성공 종료코드/출력 계약을 유지하는지
