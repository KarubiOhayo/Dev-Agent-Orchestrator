# H-035 fallback-warning 실행량/체인 커버리지 데이터 생성(traffic seeding) 부트스트랩

Owner: WT-35 (`codex/h035-fallback-warning-traffic-seeding-workload-bootstrap`)
Priority: High

## 목표
- H-034 이후에도 `resumeDecision=KEEP_FROZEN`이 유지되는 상황에서, 운영 문서 필드 추가보다 실제 run-state 데이터 생성(트래픽 시딩)을 우선한다.
- `LOW_TRAFFIC`와 `CHAIN_COVERAGE_GAP` 신호 개선의 최소 증거(runId + 체인 이벤트)를 확보한다.
- `DOC/REVIEW actualChainRuns=0` 상태를 깨기 위한 재현 가능한 실행 루틴을 코드/문서로 고정한다.

## 작업 범위
- 워크로드 실행 스크립트(신규)
  - `scripts/seed-fallback-warning-workload.sh`
- 워크로드 사용 가이드(신규/보강)
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기본 방향(권장):
  - 기존 CLI 기능을 조합해 run-state를 생성하는 방식으로 구현한다.
  - 신규 API/스키마/fallback-warning 판단 필드 확장은 하지 않는다.
- run-state 조회 기준(고정):
  - 기본 SQLite 경로: `storage/devagent.db` (`devagent.run-state.jdbc-url` 기본값)
  - 기본 테이블: `runs`, `run_events`
  - `run_events` 핵심 컬럼: `run_id`, `event_type`, `payload`, `created_at`
- 스크립트 요구사항:
  - repo root에서 실행 가능해야 한다.
  - 실행 파라미터를 env로 제어 가능해야 한다(예: `SEED_DIRECT_RUNS`, `SEED_CHAIN_RUNS`, `SEED_APPLY`, `SEED_MODE`).
  - 최소 2개 워크로드를 제공한다.
    1. Code direct 호출 시드
       - 예: `./devagent generate -u "<request>" --chain-to-doc false --chain-to-review false`
    2. Spec -> Code -> Doc/Review 체인 호출 시드
       - 예: `./devagent spec -u "<request>" --chain-to-code true --code-chain-to-doc true --code-chain-to-review true --code-chain-failure-policy PARTIAL_SUCCESS --spec-output-path "storage/devagent-specs/seed-<ts>-<n>.json"`
  - 각 실행의 `runId`, 종료코드, 핵심 신호(체인 여부)를 요약 출력하고 로그 파일로 남긴다.
  - 스크립트는 `jq` 의존 없이 동작해야 한다.
    - `--json=true` 출력과 `python` 표준 라이브러리(`json`, `sqlite3`)만으로 runId/이벤트를 추출한다.
  - 실패 시 전체 중단 여부를 옵션으로 제어할 수 있어야 한다(기본: fail-fast).
- runId 매핑 규칙(필수):
  - `generate --json`의 top-level `runId`는 `codeRunId`로 간주한다.
  - `spec --json`의 top-level `runId`는 `specRunId`로 간주한다.
  - Spec 체인 실행의 `codeRunId`는 `run_events`에서 `specRunId + CHAIN_CODE_DONE(payload: codeRunId=...)`로 추출한다.
  - `CHAIN_DOC_*`, `CHAIN_REVIEW_*` 검증은 반드시 `codeRunId` 기준으로 수행한다.
  - 가능하면 `CHAIN_DOC_DONE(payload: docRunId=...)`, `CHAIN_REVIEW_DONE(payload: reviewRunId=...)`도 추출해 함께 보고한다.
- 집계/검증:
  - 게이트/델타 재집계 기준 문서는 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`와 `coordination/REPORTS/H-033-result.md`를 우선 참조한다(KST 기준 유지).
  - 최신 14일/최근 7일 기준으로 기존 게이트 집계 방식은 유지한다.
  - `run_events`에서 `CHAIN_DOC_*`, `CHAIN_REVIEW_*` 이벤트 발생 여부를 확인한다(반드시 `codeRunId` 기준).
  - 최소 1회 실행 결과에 대해 `before/after` 비교(실행량, chainRuns, evidence freshness 관련 수치)를 결과 보고에 포함한다.
- 문서 반영:
  - `docs/cli-quickstart.md`에 "fallback-warning traffic seeding" 실행 섹션을 추가한다.
  - `docs/code-agent-api.md` H-035 섹션에는 "문서 필드 확장보다 데이터 생성 우선" 원칙과 운영 절차를 명시한다.
  - `KEEP_FROZEN` 판정 필드(`signalRecoveryEvidenceLedger[]`, `evidenceAccumulationSummary[]`, `evidenceFreshnessSummary[]`)는 유지하되 신규 필드 추가는 금지한다.
- 제약:
  - 공통 승인 대상 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 금지.
  - Spec 체인 호출 시 `--spec-output-path`를 `storage/devagent-specs/` 하위로 명시해 `.devagent/specs/` 작업트리 누적을 방지한다.
  - 외부 키/비용 제약으로 실run 생성이 불가하면, 막힌 사유/증빙을 결과 보고에 남기고 대체 실행 전략(로컬 모의 입력 시나리오)을 부록으로 제시한다.

## 수용 기준
1. `scripts/seed-fallback-warning-workload.sh`가 추가되고, direct/chain 시딩 명령을 재현 가능하게 실행한다.
2. 결과 보고에 최소 1건 이상의 유효 runId가 포함되고, `specRunId -> codeRunId -> docRunId/reviewRunId(가능 시)` 매핑 표가 포함된다.
3. 결과 보고에 `CHAIN_DOC_*` 또는 `CHAIN_REVIEW_*` 이벤트 확인 근거가 `codeRunId` 기준으로 포함된다.
4. 결과 보고에 최신 14일 게이트 4개 + 최근 7일 대비 변화량(`executionGapDelta`, `chainShareGapDelta`)이 포함된다.
5. `docs/cli-quickstart.md`, `docs/code-agent-api.md`가 H-035 목적(트래픽 생성 우선)과 실행 절차로 동기화된다.
6. fallback-warning 계약 필드 신규 추가 없이 기존 계약을 유지한다.
7. 코드/설정 금지 파일 변경이 없다.
8. `./gradlew clean test --no-daemon` 통과.

## 비범위
- fallback warning 임계치/알림 룰 수치 조정
- run-state 이벤트 스키마 변경
- 자동 커밋/PR/웹훅 전송 자동화

## 보고서
- 완료 시 `coordination/REPORTS/H-035-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-035-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - 시딩 스크립트 실행 명령(파라미터 포함) 및 요약 결과
  - 생성된 runId 목록 + direct/chain 분류 표
  - `specRunId -> codeRunId -> docRunId/reviewRunId(가능 시)` 매핑 표
  - `run_events` 근거(`CHAIN_CODE_DONE`, `CHAIN_DOC_*`, `CHAIN_REVIEW_*`) 확인 표
  - 최신 14일 게이트 4개 PASS/FAIL + 최근 7일/직전 7일 비교
  - `resumeDecision` 단일 판정 및 근거
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크 및 차기 액션
