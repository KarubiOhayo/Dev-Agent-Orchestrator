# [H-025] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-025-spec-code-chain-propagate-doc-review-cli.md`
- main relay: `coordination/RELAYS/H-025-main-to-executor.md`
- result: `coordination/REPORTS/H-025-result.md`

## 구현 요약
- 핵심 변경:
  - `SpecGenerateRequest`에 Code 체인 확장 필드 추가(`codeChainToDoc`, `codeDocUserRequest`, `codeChainToReview`, `codeReviewUserRequest`, `codeChainFailurePolicy`)
  - `SpecCodeChainService`에서 신규 필드를 `CodeGenerateRequest`로 전파
  - CLI `generate/spec`에 신규 체인 옵션 노출 및 매핑
  - CLI human/json summary에 `chainedDoc`, `chainedReview`, `chainFailures(count)` 추가
  - CLI json `data.chainFailures[]` 구조화 배열 추가
  - 문서(`docs/cli-quickstart.md`, `docs/code-agent-api.md`)에 원샷 체이닝 옵션/예시 반영
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecGenerateRequest.java`
  - `src/main/java/me/karubidev/devagent/agents/spec/SpecCodeChainService.java`
  - `src/main/java/me/karubidev/devagent/cli/DevAgentCliArguments.java`
  - `src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
  - `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
  - `src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`
  - `src/test/java/me/karubidev/devagent/cli/DevAgentCliArgumentsTest.java`
  - `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
  - `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `coordination/REPORTS/H-025-result.md`
  - `coordination/RELAYS/H-025-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. Spec -> Code 체인 옵션 전파(`codeChainToDoc`/`codeChainToReview`/`codeChainFailurePolicy`)가 구현/테스트에서 일치하는지
2. CLI 신규 옵션(`generate`, `spec` prefix) 파싱/매핑 시 unknown option, enum/boolean 처리 회귀가 없는지
3. CLI 출력 계약(summary + `chainFailures[]`)이 기존 필드(`summary/fileResults`)와 호환되게 유지되는지

## 알려진 리스크 / 오픈 이슈
- `PARTIAL_SUCCESS`에서도 클라이언트가 `chainFailures[]`를 읽지 않으면 체인 실패를 간과할 수 있음(문서/출력으로 완화)
- JSON 소비자가 기존 키만 파싱할 경우 신규 `data.chainFailures` 활용이 제한될 수 있음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-025-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
