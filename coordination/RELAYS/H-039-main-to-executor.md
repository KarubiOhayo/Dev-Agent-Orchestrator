# [H-039] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-039-fallback-warning-keep-frozen-resume-readiness-followup-check.md`
- 참고 result: `coordination/REPORTS/H-038-result.md`
- 참고 review: `coordination/REPORTS/H-038-review.md`
- 참고 relay: `coordination/RELAYS/H-038-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-039-fallback-warning-keep-frozen-resume-readiness-followup-check.md`
6. `coordination/REPORTS/H-038-result.md`, `coordination/REPORTS/H-038-review.md`, `coordination/RELAYS/H-038-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`

## 작업 범위
- 수정/추가 허용 파일(예시):
  - `scripts/seed-fallback-warning-workload.sh` (필요 시)
  - `docs/cli-quickstart.md` (필요 시 최소 동기화)
  - `docs/code-agent-api.md` (필요 시 최소 동기화)
  - `docs/PROJECT_OVERVIEW.md` (필요 시 최소 동기화)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-039-result.md`
- `coordination/RELAYS/H-039-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-038 기준 미충족 신호(`INSUFFICIENT_SAMPLE_RATIO=0.9286`, `SUFFICIENT_DAYS=1`, 최근 3일 평균 전체 `parseEligibleRunCount=26.3333`)가 지속 중이므로, H-039에서는 누적 실행 결과와 readiness 추세 판독을 함께 제시할 것.
- fail-fast 체인 경로의 non-zero 실패(직전 라운드 4건) 원인 분류(`TEMPERATURE_UNSUPPORTED`, `MODEL_NOT_FOUND_OR_UNAVAILABLE`, `ALL_CANDIDATES_FAILED`, `OTHER`)를 동일 기준으로 재집계해 재발 빈도를 비교할 것.
- 정책 고정 항목(운영 계약 필드/임계치/`INSUFFICIENT_SAMPLE` 제외 규칙/단일 판정 계약)은 변경하지 말고, `RESUME_H024|KEEP_FROZEN` 판단 근거만 최신 데이터로 갱신할 것.
