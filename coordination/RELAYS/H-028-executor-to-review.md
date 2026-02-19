# [H-028] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-028-cli-guardrail-consumer-readiness-check.md`
- main relay: `coordination/RELAYS/H-028-main-to-executor.md`
- result: `coordination/REPORTS/H-028-result.md`

## 구현 요약
- 핵심 변경:
  - JSON 출력 `data.guardrailTriggered` 추가(`generate/spec` 공통)
  - `guardrailTriggered=true` 조건 고정: `fail-on-chain-failures=true && chainFailures>0`
  - human 경고 문구에 가드레일 상태(`guardrail=enabled|disabled`) 표시
  - `DevAgentCliRunner`에서 formatter로 가드레일 옵션값 전달 정합화
  - `DevAgentCliRunnerTest`에 handoff 요구 3케이스(`true+실패`, `false+실패`, `true+무실패`) + `spec` JSON 가드레일 케이스 추가
  - `CliResultFormatterTest`에 JSON 필드/경고 문구 회귀 검증 보강
  - `docs/cli-quickstart.md`에 비교표 + shell/GitHub Actions 샘플 + 안티패턴 추가
  - `docs/code-agent-api.md`에 CLI 소비 규약(필드+종료코드 동시 확인) 보강
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
  - `src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
  - `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
  - `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `coordination/REPORTS/H-028-result.md`
  - `coordination/RELAYS/H-028-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests "me.karubidev.devagent.cli.CliResultFormatterTest" --tests "me.karubidev.devagent.cli.DevAgentCliRunnerTest"`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `CliResultFormatter`의 `data.guardrailTriggered`가 `generate/spec` 상위 `data`에만 추가되고 기존 JSON 필드 호환을 깨지 않는지
2. `DevAgentCliRunner`의 종료코드 `3/0` 분기와 `guardrailTriggered` 분기가 동일 조건으로 정렬되어 있는지
3. `docs/cli-quickstart.md` 샘플 파이프라인이 `exit code 3`을 실패로 승격하는 체크리스트를 충분히 담고 있는지

## 알려진 리스크 / 오픈 이슈
- 가드레일 opt-in 정책은 유지되어, 소비자가 `--fail-on-chain-failures=true`를 사용하지 않으면 종료코드 기반 강제 실패는 동작하지 않음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-028-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
