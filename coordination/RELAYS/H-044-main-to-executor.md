# [H-044] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-044-fallback-warning-keep-frozen-resume-readiness-next-check.md`
- 참고 close-out result: `coordination/REPORTS/H-043-result.md`
- 참고 close-out review: `coordination/REPORTS/H-043-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-043-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-02-24.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `docs/OBSERVABILITY_FALLBACK_WARNING.md`
4. `coordination/TASK_BOARD.md`
5. `coordination/DECISIONS.md`
6. `coordination/HANDOFFS/H-044-fallback-warning-keep-frozen-resume-readiness-next-check.md`
7. `coordination/REPORTS/H-043-result.md`, `coordination/REPORTS/H-043-review.md`, `coordination/RELAYS/H-043-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-02-24.md`
8. `coordination/REPORTS/H-036-result.md`, `coordination/REPORTS/H-037-result.md`, `coordination/REPORTS/H-038-result.md`, `coordination/REPORTS/H-039-result.md`, `coordination/REPORTS/H-042-result.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
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
- fail-fast 실패 케이스는 non-zero 종료코드로 보고서에 증빙한다.

## 완료 산출물
- `coordination/REPORTS/H-044-result.md`
- `coordination/RELAYS/H-044-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- H-043 기준 `INSUFFICIENT_SAMPLE_RATIO=0.8571`, `SUFFICIENT_DAYS=2`로 재개 게이트가 아직 미충족 상태다. H-044에서는 동일 산식으로 최신 게이트를 재집계하고 `unmetReadinessSignals`를 명확히 고정할 것.
- 최근 3일 평균 전체 모수(`parseEligibleRunCount=24.3333`)가 기준(`>=32`)을 하회하므로 실행 공백이 지표 변동성에 미치는 영향을 함께 분석할 것.
- fail-fast 체인 실패 원인 분류(`TEMPERATURE_UNSUPPORTED`, `MODEL_NOT_FOUND_OR_UNAVAILABLE`, `ALL_CANDIDATES_FAILED`, `OTHER`)를 동일 기준으로 집계해 이전 라운드 대비 재발 여부를 제시할 것.
- fallback-warning 용어는 output parsing fallback 경고만 의미한다. 모델 라우팅 fallback과 혼합 해석하지 않는다(`docs/OBSERVABILITY_FALLBACK_WARNING.md` 기준).
