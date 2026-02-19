# H-028 결과 보고서 (CLI 가드레일 실사용성 점검)

## 상태
- 현재 상태: **완료 (구현 + 테스트 + 문서 반영 + 게이트 통과)**
- 실행일(KST): `2026-02-19`

## 변경 파일 목록
- `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
- `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
- `docs/cli-quickstart.md`
- `docs/code-agent-api.md`
- `coordination/REPORTS/H-028-result.md`
- `coordination/RELAYS/H-028-executor-to-review.md`

## 구현 요약
### A) JSON 소비 신호 보강 (`data.guardrailTriggered`)
- JSON 출력 `data.guardrailTriggered`를 `generate/spec` 모두에 추가했습니다.
- `guardrailTriggered=true` 조건을 `fail-on-chain-failures=true && chainFailures>0`로 고정했습니다.
- 기존 필드(`data.summary`, `data.hasChainFailures`, `data.chainFailures[]`, `data.fileResults`)는 그대로 유지했습니다.

### B) human 출력 가시성 보강
- `chainFailures>0` 경고 문구에 `guardrail=enabled|disabled` 상태를 명시했습니다.
- 가드레일 비활성 시에는 기존 안내(`--fail-on-chain-failures=true` 사용 시 exit code 3)를 유지했습니다.
- 가드레일 활성 시에는 `exit code 3` 반환을 문구에 직접 표시했습니다.

### C) 자동화/CI 소비 문서화
- `docs/cli-quickstart.md`
  - 기본 모드 vs 가드레일 모드 비교표(종료코드/필수 점검 항목) 추가
  - shell/GitHub Actions 샘플 파이프라인(`exit code 3` 처리) 추가
  - 안티패턴(`continue-on-error`, 종료코드 무시, `chainFailures[]` 미확인) 명시
  - JSON 예시에 `data.guardrailTriggered` 반영
- `docs/code-agent-api.md`
  - CLI 소비 규약에 `data.hasChainFailures` + `data.guardrailTriggered` + 종료코드 동시 확인 패턴 보강

## JSON `guardrailTriggered` 계약 검증 결과
- `generate`
  - `fail-on=true + chainFailures>0` -> `exit 3`, `guardrailTriggered=true`
  - `fail-on=false(default) + chainFailures>0` -> `exit 0`, `guardrailTriggered=false`
  - `fail-on=true + chainFailures=0` -> `exit 0`, `guardrailTriggered=false`
- `spec`
  - `fail-on=true + chainedCode.chainFailures>0` -> `exit 3`, `guardrailTriggered=true`
  - `fail-on=false(default) + chainedCode.chainFailures>0` -> `exit 0`, `guardrailTriggered=false`

## 종료코드 매트릭스(`generate/spec`) 검증 결과
- `generate`: `true+failures>0 => 3`, `false+failures>0 => 0`, `true+failures=0 => 0`
- `spec`: `true+failures>0 => 3`, `default(false)+failures>0 => 0`

## human 출력 가시성 보강 검증 결과
- `chainFailures>0`일 때 경고 문구에 `guardrail=disabled`/`guardrail=enabled`가 각각 표시됨을 단위 테스트로 검증했습니다.

## 테스트 명령 및 결과
- 선택 검증:
  - 명령:
    - `./gradlew test --no-daemon --tests "me.karubidev.devagent.cli.CliResultFormatterTest" --tests "me.karubidev.devagent.cli.DevAgentCliRunnerTest"`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 가드레일은 opt-in(`default=false`)이므로, 소비자가 옵션을 활성화하지 않으면 `chainFailures[]`가 있어도 종료코드는 `0`입니다.
- CI가 종료코드만 보고 JSON 본문(`chainFailures[]`)을 무시하면 기본 모드에서 체인 실패를 놓칠 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `CliResultFormatter`의 `guardrailTriggered` 계산이 `generate/spec` 모두에서 handoff 조건(`true + failures>0`)과 일치하는지
2. `DevAgentCliRunner`에서 formatter 전달값(`failOnChainFailures`)과 실제 종료코드(`3/0`) 분기가 어긋나지 않는지
3. 문서의 체크리스트/샘플 파이프라인이 `exit code 3` 처리 누락을 방지하는 형태로 충분한지
