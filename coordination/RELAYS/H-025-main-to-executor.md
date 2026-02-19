# [H-025] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-025-spec-code-chain-propagate-doc-review-cli.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`
- 참고 docs: `docs/PROJECT_OVERVIEW.md`, `docs/code-agent-api.md`, `docs/cli-quickstart.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-025-spec-code-chain-propagate-doc-review-cli.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - src/main/java/me/karubidev/devagent/agents/spec/SpecGenerateRequest.java
  - src/main/java/me/karubidev/devagent/agents/spec/SpecCodeChainService.java
  - src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java
  - src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java
  - docs/cli-quickstart.md
  - docs/code-agent-api.md
  - src/test/java/** (관련 테스트만)
- 수정 금지(공통 파일):
  - src/main/resources/application.yml
  - build.gradle, settings.gradle, gradle/wrapper/**
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-025-result.md`
- `coordination/RELAYS/H-025-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- Spec -> Code 체인에 “Code의 chain 옵션”이 제대로 전파되는지(특히 chainFailurePolicy 포함)
- CLI 옵션 노출 후, unknown option / alias 충돌이 없는지
- CLI JSON 출력 호환(기존 summary/fileResults 유지) + chainFailures 노출이 누락되지 않는지
- PARTIAL_SUCCESS 사용 시 chainFailures 확인을 CLI/문서에서 명확히 안내하는지
