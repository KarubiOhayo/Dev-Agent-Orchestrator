# [H-026] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-026-spec-cli-chain-e2e-contract-hardening.md`
- main relay: `coordination/RELAYS/H-026-main-to-executor.md`
- result: `coordination/REPORTS/H-026-result.md`

## 구현 요약
- 핵심 변경:
  - `SpecAgentServiceTest`에 `chainToCode + PARTIAL_SUCCESS` 응답 계약 검증 추가
    - `chainedCodeResult` 존재
    - `chainedCodeResult.chainedDocResult` 존재
    - `chainedCodeResult.chainFailures[]` 구조(`agent`, `failedStage`, `errorMessage`) 검증
  - `SpecCodeChainServiceTest`에 default 회귀 추가
    - `chainToCode=false` 시 체인 미실행/`CodeAgentService` 미호출 검증
  - `DevAgentCliRunnerTest`에 CLI `generate/spec` 계약 검증 보강
    - human 출력: `chainedDoc`, `chainedReview`, `chainFailures` + failure detail
    - json 출력: `data.summary` + `data.chainFailures[]` + `data.chainedCode.summary.chainFailures`
    - 신규 옵션 미사용 시 기본값(`false`, `FAIL_FAST`) 유지 검증
- 변경 파일:
  - `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java`
  - `src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`
  - `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
  - `coordination/REPORTS/H-026-result.md`
  - `coordination/RELAYS/H-026-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests "me.karubidev.devagent.agents.spec.SpecAgentServiceTest" --tests "me.karubidev.devagent.agents.spec.SpecCodeChainServiceTest" --tests "me.karubidev.devagent.cli.DevAgentCliRunnerTest"`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `SpecAgentServiceTest.generateIncludesChainedCodeResultAndChainFailuresForPartialSuccess`가 handoff의 `PARTIAL_SUCCESS + chainFailures[]` 계약 요구를 충족하는지
2. `DevAgentCliRunnerTest`의 `generate/spec` human/json 검증이 CLI 계약(`chainedDoc`, `chainedReview`, `chainFailures`, `data.chainFailures[]`)을 충분히 커버하는지
3. default 회귀 테스트(`runChainSkipsWhenChainToCodeIsDisabled`, `runGenerateKeepsDefaultChainOptionsWhenNotProvided`, `runSpecKeepsDefaultCodeChainOptionsWhenNotProvided`)가 하위호환 리스크를 충분히 차단하는지

## 알려진 리스크 / 오픈 이슈
- 소비 클라이언트가 `chainFailures[]`를 무시하면 여전히 체인 실패를 간과할 수 있음(출력/테스트 계약으로 완화)

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-026-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
