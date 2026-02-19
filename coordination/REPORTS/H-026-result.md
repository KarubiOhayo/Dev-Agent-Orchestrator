# H-026 결과 보고서 (Spec/CLI 원샷 체이닝 E2E 계약 테스트 보강)

## 상태
- 현재 상태: **완료 (테스트 보강 + 게이트 통과)**
- 실행일(KST): `2026-02-19`

## 변경 파일
- `src/test/java/me/karubidev/devagent/agents/spec/SpecAgentServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
- `coordination/REPORTS/H-026-result.md`
- `coordination/RELAYS/H-026-executor-to-review.md`

## 구현 요약
- `SpecAgentService` 경로에서 `chainToCode + PARTIAL_SUCCESS` 응답 계약을 고정하는 테스트를 추가했습니다.
  - `chainedCodeResult` 존재 여부
  - `chainedCodeResult.chainedDocResult` 노출 여부
  - `chainedCodeResult.chainFailures[]`(`agent`, `failedStage`, `errorMessage`) 노출 여부
- `SpecCodeChainService` 기본 경로 회귀 테스트를 추가했습니다.
  - 신규 옵션 미사용(`chainToCode=false`) 시 체인이 실행되지 않고 `CodeAgentService` 호출이 발생하지 않음을 검증
- CLI runner 레벨에서 `generate/spec` 경로의 출력 계약 검증을 보강했습니다.
  - `PARTIAL_SUCCESS` 응답 시 human 출력의 `chainedDoc`, `chainedReview`, `chainFailures` 및 failure detail 노출 확인
  - JSON 출력의 `data.summary` + `data.chainFailures[]` 계약 검증
  - 신규 옵션 미사용(default) 시 `FAIL_FAST`, `false`, `null` 기본값 유지 회귀 검증

## E2E 성격 테스트 시나리오/검증 포인트
1. Spec -> Code 체인(`codeChainToDoc=true`, `codeChainToReview=true`, `codeChainFailurePolicy=PARTIAL_SUCCESS`)
- `SpecAgentServiceTest.generateIncludesChainedCodeResultAndChainFailuresForPartialSuccess`
- 검증: `chainedCodeResult` 하위 `chainedDocResult` + `chainFailures[]`가 동시에 응답에 반영됨

2. CLI `generate/spec` 경로 출력 계약
- `DevAgentCliRunnerTest.runGeneratePrintsHumanSummaryAndChainFailuresForPartialSuccess`
- `DevAgentCliRunnerTest.runGeneratePrintsJsonSummaryAndChainFailuresForPartialSuccess`
- `DevAgentCliRunnerTest.runSpecPrintsHumanSummaryAndChainFailuresForPartialSuccess`
- `DevAgentCliRunnerTest.runSpecPrintsJsonSummaryAndChainFailuresForPartialSuccess`
- 검증: human summary(`chainedDoc`, `chainedReview`, `chainFailures`) + JSON(`data.summary`, `data.chainFailures[]`) 계약 고정

3. 신규 옵션 미사용(default) 회귀
- `SpecCodeChainServiceTest.runChainSkipsWhenChainToCodeIsDisabled`
- `DevAgentCliRunnerTest.runGenerateKeepsDefaultChainOptionsWhenNotProvided`
- `DevAgentCliRunnerTest.runSpecKeepsDefaultCodeChainOptionsWhenNotProvided`
- 검증: 기존 기본 동작(`chain off`, `FAIL_FAST`) 유지

## `PARTIAL_SUCCESS` + `chainFailures[]` 검증 결과
- API 응답 객체(`SpecGenerateResponse.chainedCodeResult`)에서 `chainFailures[]`가 누락 없이 노출됨을 확인
- CLI `generate/spec` 경로에서 human/json 모두 `chainFailures` 정보를 확인 가능함을 검증

## CLI human/json 출력 계약 검증 결과
- human:
  - `chainedDoc`, `chainedReview`, `chainFailures` summary 출력 확인
  - `chain failures` 상세(`agent`, `stage`, `message`) 출력 확인
- json:
  - `data.summary.chainedDoc`, `data.summary.chainedReview`, `data.summary.chainFailures` 확인
  - `data.chainFailures[]` 구조화 배열(`agent`, `failedStage`, `errorMessage`) 확인
  - `spec` 경로에서 `data.chainedCode.summary.chainFailures` 동기화 확인

## 기본 동작(신규 옵션 미사용) 회귀 검증 결과
- `generate`에서 신규 체인 옵션 미사용 시:
  - `chainToDoc=false`, `chainToReview=false`, `chainFailurePolicy=FAIL_FAST`
- `spec`에서 code-chain 확장 옵션 미사용 시:
  - `codeChainToDoc=false`, `codeChainToReview=false`, `codeChainFailurePolicy=FAIL_FAST`
- `SpecCodeChainService`에서 `chainToCode=false` 시 코드 체인 미실행 확인

## 테스트 결과
- 사전 검증:
  - 명령: `./gradlew test --no-daemon --tests "me.karubidev.devagent.agents.spec.SpecAgentServiceTest" --tests "me.karubidev.devagent.agents.spec.SpecCodeChainServiceTest" --tests "me.karubidev.devagent.cli.DevAgentCliRunnerTest"`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- CLI/API 소비자가 여전히 `chainFailures[]`를 읽지 않는 경우, `PARTIAL_SUCCESS`의 체인 실패를 간과할 수 있음
- 이번 라운드는 계약 검증 테스트 보강 범위로 제한되어 실제 외부 클라이언트 소비 로직까지는 포함하지 않음

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `SpecAgentServiceTest`의 `PARTIAL_SUCCESS` 계약 검증이 `chainedCodeResult` 하위 구조를 충분히 고정하는지
2. `DevAgentCliRunnerTest`의 human/json 출력 검증이 `generate/spec` 양 경로를 모두 커버하는지
3. default 회귀 테스트가 신규 옵션 미사용 경로의 하위호환 리스크를 충분히 차단하는지
