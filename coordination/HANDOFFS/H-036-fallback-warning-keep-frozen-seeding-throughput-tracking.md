# H-036 fallback-warning `KEEP_FROZEN` seeding throughput 추적 점검

Owner: WT-36 (`codex/h036-fallback-warning-keep-frozen-seeding-throughput-tracking`)
Priority: High

## 목표
- H-035.1에서 보강한 fail-fast 종료코드 신뢰성을 유지한 상태로 반복 시딩 실행량을 누적한다.
- 최신 14일 게이트 4종 + 최근 7일/직전 7일 delta를 재집계해 `RESUME_H024|KEEP_FROZEN` 단일 판정을 갱신한다.
- H-024 Frozen/Backlog 해제 판단에 필요한 runId/체인 이벤트 증거를 추가 확보한다.

## 작업 범위
- 시딩 실행/보조 개선(필요 시)
  - `scripts/seed-fallback-warning-workload.sh`
- 운영 가이드 동기화(필요 시 최소 범위)
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md`
  - `coordination/HANDOFFS/H-035-1-fallback-warning-seeding-fail-fast-exitcode-hardening.md`
  - `coordination/REPORTS/H-035-result.md`
  - `coordination/REPORTS/H-035-1-result.md`
  - `coordination/REPORTS/H-035-1-review.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`
- 실행 정책:
  - 기본 실행 목표는 direct 최소 6회 + chain 최소 3회(총 9회)로 설정한다.
  - `SEED_FAIL_FAST=true`를 기본으로 사용한다.
  - 외부 키/비용/환경 제약으로 목표 실행이 불가하면, 중단 사유/증빙/대체 실행 계획을 결과 보고에 남긴다.
- 데이터 검증 정책:
  - `specRunId -> CHAIN_CODE_DONE -> codeRunId -> CHAIN_DOC_DONE/CHAIN_REVIEW_DONE` 매핑을 유지한다.
  - 최신 14일 게이트 4종(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)을 재집계한다.
  - 최근 7일/직전 7일 `executionGapDelta`, `chainShareGapDelta`를 재산출한다.
  - 최근 3일 평균 전체 `parseEligibleRunCount`를 포함한다.
- 정책 고정:
  - fallback-warning 운영 계약 필드(`signalRecoveryEvidenceLedger[]`, `evidenceAccumulationSummary[]`, `evidenceFreshnessSummary[]`)는 추가/삭제/이름 변경 없이 유지한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.

## 수용 기준
1. direct/chain 시딩 실행 결과가 결과 보고에 포함되고, 최소 1건 이상의 유효 chain 증거(`CHAIN_DOC_DONE` 또는 `CHAIN_REVIEW_DONE`)가 `codeRunId` 기준으로 제시된다.
2. 기본 목표(총 9회) 달성 여부가 명시되고, 미달 시 사유/증빙/대체 계획이 함께 보고된다.
3. 최신 14일 게이트 4종 PASS/FAIL + 최근 7일/직전 7일 delta(`executionGapDelta`, `chainShareGapDelta`)가 포함된다.
4. `resumeDecision` 단일 판정(`RESUME_H024|KEEP_FROZEN`)과 `unmetReadinessSignals`가 포함된다.
5. fail-fast 실행 중 실패가 발생한 경우, 해당 케이스가 non-zero 종료코드로 처리되었음이 보고된다.
6. 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경이 없다.
7. `./gradlew clean test --no-daemon` 통과.

## 비범위
- fallback warning 임계치/알림 룰 수치 조정
- run-state 이벤트 스키마 변경
- 자동 커밋/PR/웹훅 전송 자동화

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 Main-Control 사전 승인 요청만 남긴다.
- Spec 체인 호출 시 `--spec-output-path`는 `storage/devagent-specs/` 하위로 고정한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-036-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-036-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - 시딩 실행 명령(파라미터 포함) 및 요약 결과
  - 생성된 runId 목록 + direct/chain 분류 표
  - `specRunId -> codeRunId -> docRunId/reviewRunId(가능 시)` 매핑 표
  - `run_events` 근거(`CHAIN_CODE_DONE`, `CHAIN_DOC_*`, `CHAIN_REVIEW_*`) 확인 표
  - 최신 14일 게이트 4개 PASS/FAIL + 최근 7일/직전 7일 비교
  - `resumeDecision` 단일 판정 및 `unmetReadinessSignals`
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크 및 차기 액션
