# [H-027] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-027-cli-partial-success-consumption-guardrail.md`
- main relay: `coordination/RELAYS/H-027-main-to-executor.md`
- result: `coordination/REPORTS/H-027-result.md`

## 구현 요약
- 핵심 변경:
  - `generate/spec` 공통 옵션 `--fail-on-chain-failures=<true|false>` 추가(기본 `false`)
  - `fail-on=true` + `chainFailures[] > 0`에서 종료코드 `3` 강제(`DevAgentCliRunner`)
  - human 출력에서 `chainFailures > 0` 시 경고 1줄 추가
  - JSON 출력에 `data.hasChainFailures` 보조 필드 추가(기존 `data.summary`, `data.chainFailures[]` 유지)
  - `DevAgentCliRunnerTest`/`DevAgentCliArgumentsTest`/`CliResultFormatterTest` 보강
  - `docs/cli-quickstart.md`, `docs/code-agent-api.md` 문서 정합화
- 변경 파일:
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

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests "me.karubidev.devagent.cli.DevAgentCliArgumentsTest" --tests "me.karubidev.devagent.cli.CliResultFormatterTest" --tests "me.karubidev.devagent.cli.DevAgentCliRunnerTest"`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `DevAgentCliRunner`의 종료코드 계약이 handoff 요구사항과 일치하는지(`true + failures>0 -> 3`, 나머지 -> 기존 동작)
2. `CliResultFormatter`의 `data.hasChainFailures` 추가가 기존 JSON 소비 계약(`data.summary`, `data.chainFailures[]`)을 깨지 않는지
3. human 경고 출력 추가가 `generate/spec` 양 경로에서 기대대로 노출되며 기존 출력 테스트와 충돌하지 않는지

## 알려진 리스크 / 오픈 이슈
- 가드레일은 opt-in이므로 미사용 시 종료코드 기반 실패 강제는 여전히 동작하지 않음(기본 하위호환 유지 설계)

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-027-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
