# H-047 fallback-warning `KEEP_FROZEN` resume readiness follow-up check

Owner: WT-47 (`codex/h047-fallback-warning-keep-frozen-resume-readiness-followup-check`)
Priority: High

## 목표
- H-046에서 유지된 `KEEP_FROZEN` 판정을 바탕으로 최신 증거를 추가 누적하고 최신 14일/7일 게이트를 동일 산식으로 재집계한다.
- H-036~H-039/H-042/H-043/H-044/H-045/H-046/H-047 readiness 추세를 단일 표로 비교해 개선/정체/악화 판독 근거를 고정한다.
- H-046에서 해소한 배치별 감사 추적성(`SEED_TIMESTAMP` 분리)을 유지하면서, H-046 실행일(`2026-03-09` KST)과 다른 KST 날짜 창의 신규 시딩 증거를 확보해 `SUFFICIENT_DAYS`/`INSUFFICIENT_SAMPLE_RATIO` 개선 가능성을 검증한다.

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
  - `coordination/REPORTS/H-046-result.md`
  - `coordination/REPORTS/H-046-review.md`
  - `coordination/RELAYS/H-046-review-to-main.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-10.md`
  - `coordination/REPORTS/H-036-result.md`
  - `coordination/REPORTS/H-037-result.md`
  - `coordination/REPORTS/H-038-result.md`
  - `coordination/REPORTS/H-039-result.md`
  - `coordination/REPORTS/H-042-result.md`
  - `coordination/REPORTS/H-043-result.md`
  - `coordination/REPORTS/H-044-result.md`
  - `coordination/REPORTS/H-045-result.md`
- 실행 정책:
  - 신규 시딩 증거는 H-046 실행일(`2026-03-09` KST)과 다른 KST 날짜 창에서 최소 1회 이상 확보하는 것을 우선한다.
  - fresh 시딩을 수행할 경우 기본 실행 목표는 direct 최소 6회 + chain 최소 3회(총 9회)로 설정한다.
  - `SEED_FAIL_FAST=true`를 기본으로 사용한다.
  - 진단 배치(`SEED_DIRECT_RUNS=1`, `SEED_CHAIN_RUNS=1`)를 1회 수행한 뒤 본 실행 배치를 진행한다.
  - 진단/direct/chain 각 배치는 서로 다른 `SEED_TIMESTAMP`(또는 동등한 고유 suffix)를 사용해 `seed-*.log`, `seed-*-records.jsonl`, `seed-*-before.json`, `seed-*-after.json`, `seed-*-summary.json`이 배치별로 분리되도록 한다.
  - chain 배치가 fail-fast로 중단될 경우 동일 파라미터 `SEED_CHAIN_RUNS=1` 재시도로 보강하되, 재시도 횟수 상한(권장 8회)을 적용하고 각 재시도도 고유 `SEED_TIMESTAMP`로 분리한다.
  - 같은 라운드 안에서 다중 KST 날짜 실행이 현실적으로 불가능하면, 타임스탬프를 임의로 조작하지 말고 실제 제약과 추가로 필요한 distinct compliant day 수를 결과 보고에 명시한다.
- 데이터 검증 정책:
  - `specRunId -> CHAIN_CODE_DONE -> codeRunId -> CHAIN_DOC_DONE/CHAIN_REVIEW_DONE` 매핑을 유지한다.
  - 최신 14일 게이트 4종(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)을 재집계한다.
  - 최근 7일/직전 7일 `executionGapDelta`, `chainShareGapDelta`를 재산출한다.
  - 최근 3일 평균 전체 `parseEligibleRunCount`를 포함한다.
  - 최근 14일 일자별 `actualTotalRuns`, `actualChainRuns`, `parseEligibleRunCount`, `dailyCompliance`, `sufficientSample` 여부를 표로 제시한다.
  - `INSUFFICIENT_SAMPLE_RATIO <= 0.50`, `SUFFICIENT_DAYS >= 7` 도달을 위해 추가로 필요한 최소 distinct compliant day 수를 산출한다.
- readiness 추세 검증 정책:
  - H-036/H-037/H-038/H-039/H-042/H-043/H-044/H-045/H-046/H-047 기준으로 아래 항목의 비교 표를 결과 보고에 포함한다.
    - `INSUFFICIENT_SAMPLE_RATIO`
    - `SUFFICIENT_DAYS`
    - `executionGapDelta`
    - `chainShareGapDelta`
    - 최근 3일 평균 전체 `parseEligibleRunCount`
  - 추세 판독(`개선`, `정체`, `악화`)과 다음 점검 트리거를 명시한다.
- 실패 원인 추적 정책:
  - 근거 파일(`seed-*.log`, `seed-*-records.jsonl`, `seed-*-chain-*.stdout.json`)에서 fail-fast 실패 케이스를 수집한다.
  - 실패 원인은 최소 아래 분류로 집계한다.
    - `TEMPERATURE_UNSUPPORTED`
    - `MODEL_NOT_FOUND_OR_UNAVAILABLE`
    - `ALL_CANDIDATES_FAILED`
    - `OTHER`
  - 분류별 `count`, `latestEvidenceRef`, `impact(직접/체인)`를 표로 보고한다.
- 정책 고정:
  - fallback-warning 운영 계약 필드(`signalRecoveryEvidenceLedger[]`, `evidenceAccumulationSummary[]`, `evidenceFreshnessSummary[]`)는 추가/삭제/이름 변경 없이 유지한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.

## 수용 기준
1. 신규 시딩을 수행했다면 direct/chain 실행 결과가 결과 보고에 포함되고, 최소 1건 이상의 유효 chain 증거(`CHAIN_DOC_DONE` 또는 `CHAIN_REVIEW_DONE`)가 `codeRunId` 기준으로 제시된다.
2. 기본 목표(총 9회) 달성 여부가 명시되고, 미달 시 사유/증빙/대체 계획이 함께 보고된다.
3. 진단/direct/chain 각 배치의 `SEED_TIMESTAMP`(또는 동등한 고유 suffix)가 분리돼 배치별 `summary.json`, `before/after`, `records.jsonl`, `log`를 독립 검증할 수 있다.
4. 최근 14일 일자별 `actualTotalRuns`, `actualChainRuns`, `parseEligibleRunCount`, `dailyCompliance`, `sufficientSample` 표가 포함된다.
5. `INSUFFICIENT_SAMPLE_RATIO <= 0.50`, `SUFFICIENT_DAYS >= 7` 도달을 위해 추가로 필요한 최소 distinct compliant day 수가 결과 보고에 포함된다.
6. H-036~H-039/H-042/H-043/H-044/H-045/H-046/H-047 readiness 추세 비교 표(`INSUFFICIENT_SAMPLE_RATIO`, `SUFFICIENT_DAYS`, `executionGapDelta`, `chainShareGapDelta`, 최근 3일 평균 전체 모수)가 포함된다.
7. fail-fast 실패 원인 분류 표(`rootCause`, `count`, `latestEvidenceRef`, `impact`)가 포함된다.
8. 최신 14일 게이트 4종 PASS/FAIL + 최근 7일/직전 7일 delta(`executionGapDelta`, `chainShareGapDelta`)가 포함된다.
9. `resumeDecision` 단일 판정(`RESUME_H024|KEEP_FROZEN`)과 `unmetReadinessSignals`가 포함된다.
10. fail-fast 실행 중 실패가 발생한 경우, 해당 케이스가 non-zero 종료코드로 처리되었음이 보고된다.
11. 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경이 없다.
12. `./gradlew clean test --no-daemon` 통과.

## 비범위
- fallback warning 임계치/알림 룰 수치 조정
- run-state 이벤트 스키마 변경
- 자동 커밋/PR/웹훅 전송 자동화

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 Main-Control 사전 승인 요청만 남긴다.
- Spec 체인 호출 시 `--spec-output-path`는 `storage/devagent-specs/` 하위로 고정한다.

## 보고서
- 완료 시 `coordination/REPORTS/H-047-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-047-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - 시딩 실행 명령(파라미터 포함) 및 요약 결과
  - 배치별 `SEED_TIMESTAMP`와 해당 산출물(`log`, `records.jsonl`, `before/after`, `summary.json`) 경로
  - 생성된 runId 목록 + direct/chain 분류 표
  - `specRunId -> codeRunId -> docRunId/reviewRunId(가능 시)` 매핑 표
  - `run_events` 근거(`CHAIN_CODE_DONE`, `CHAIN_DOC_*`, `CHAIN_REVIEW_*`) 확인 표
  - 최근 14일 일자별 실행 분포 표(`actualTotalRuns`, `actualChainRuns`, `parseEligibleRunCount`, `dailyCompliance`, `sufficientSample`)
  - 추가로 필요한 최소 distinct compliant day 수와 산출 근거
  - H-036~H-039/H-042/H-043/H-044/H-045/H-046/H-047 readiness 추세 비교 표 + 추세 판독
  - fail-fast 실패 원인 분류 표 + 분류별 근거 파일
  - 최신 14일 게이트 4개 PASS/FAIL + 최근 7일/직전 7일 비교
  - `resumeDecision` 단일 판정 및 `unmetReadinessSignals`
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크 및 차기 액션
