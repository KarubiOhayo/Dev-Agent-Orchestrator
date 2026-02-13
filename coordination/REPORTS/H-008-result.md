# H-008 결과 보고서 (파일 적용 경계 입력 방어 강화)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일 목록
- `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
- `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java`

## 구현 요약
- `FileApplyService` 경계 검증을 강화해 빈 경로/절대경로/`..`/invalid path 입력을 모두 `REJECTED`로 차단하도록 고정.
- 파일 시스템 I/O 실패는 경계 위반과 분리해 `ERROR` 상태로 유지.
- `CodeAgentService`에서 `targetProjectRoot` 파싱 실패를 `IllegalArgumentException`으로 명확화하고, run 시작 이후 실패를 `CODE_FAILED` 이벤트로 기록하도록 계약 보강.
- `specInputPath` 실패 시 `SPEC_INPUT_FAILED` + `CODE_FAILED` 이벤트가 유지되는 기존 계약을 회귀 테스트로 고정.
- `ModelRouter.resolve` 실패 경로(`request == null`, `agentType == null`) 테스트를 추가.

## 경계 입력 케이스별 동작 표 (정상/실패/경계)
| 구분 | 입력 케이스 | 기대 동작 | 테스트 고정 |
|---|---|---|---|
| 정상 | `src/B.java` | `WRITTEN` | `applyWritesFiles` |
| 정상 | dry-run + `src/A.java` | `DRY_RUN` (실제 파일 미작성) | `dryRunDoesNotWriteFiles` |
| 경계 | 빈 경로(`"   "`) | `REJECTED`, `empty path` | `rejectEmptyPath` |
| 경계 | 절대경로 | `REJECTED`, `absolute path is not allowed` | `rejectAbsolutePath` |
| 경계 | 경로 탈출(`../outside/A.java`) | `REJECTED`, `path traversal is not allowed` | `rejectTraversalPath` |
| 경계 | 정규화 우회(`src/../..`) | `REJECTED`, `path traversal is not allowed` | `rejectNormalizedTraversalBypass` |
| 경계 | invalid path(`bad\u0000path.java`) | `REJECTED`, `invalid path` | `rejectInvalidPath` |
| 실패 | 루트가 파일인 I/O 실패 | `ERROR` | `ioFailureIsRecordedAsError` |
| 실패(run-state) | invalid `targetProjectRoot` | `CODE_FAILED` 이벤트 기록 후 실패 | `generateRecordsCodeFailedWhenTargetProjectRootIsInvalid` |
| 실패(run-state) | absolute `specInputPath` | `SPEC_INPUT_FAILED` + `CODE_FAILED` 유지 | `generateRejectsAbsoluteSpecInputPath` |
| 실패(run-state) | traversal `specInputPath` | `SPEC_INPUT_FAILED` + `CODE_FAILED` 유지 | `generateRejectsTraversalSpecInputPath` |
| 실패(라우터) | `request == null` | `IllegalArgumentException("agentType is required")` | `resolveRejectsNullRequest` |
| 실패(라우터) | `agentType == null` | `IllegalArgumentException("agentType is required")` | `resolveRejectsNullAgentType` |

## 수용기준 충족 여부
1. 파일 적용 경계 위반 입력 차단: **충족**
2. 경계 위반 시 상태/메시지 계약 고정: **충족**
3. `targetProjectRoot`/`specInputPath` 실패 run-state 계약 회귀 방지: **충족**
4. `ModelRouter.resolve` 실패 경로 회귀 테스트 추가: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령(대상 회귀):
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.agents.code.apply.FileApplyServiceTest' --tests 'me.karubidev.devagent.agents.code.CodeAgentServiceTest' --tests 'me.karubidev.devagent.orchestration.routing.ModelRouterTest'`
  - 결과: **BUILD SUCCESSFUL**
- 실행 명령(게이트):
  - `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 본 라운드는 Code apply 경계 하드닝 중심이며, Doc/Review/Spec 서비스의 `targetProjectRoot` invalid-path 계약 통일은 비범위로 남아 있음.
- 경계 메시지 문자열은 테스트로 고정했으므로, 향후 문구 변경 시 API/테스트 동시 갱신이 필요함.

## 공통 파일 변경 승인 여부
- `application.yml` 변경: **없음**
- 공용 모델/빌드 설정 변경: **없음**
- 사전 승인 필요 항목 적용 여부: **해당 없음**
