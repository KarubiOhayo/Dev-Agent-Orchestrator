# [H-049] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-049-fallback-warning-keep-frozen-resume-readiness-followup-check.md`
- 참고 close-out result: `coordination/REPORTS/H-048-result.md`
- 참고 close-out review(착수 전 필수 확인): `coordination/REPORTS/H-048-review.md`
- 참고 close-out relay(착수 전 필수 확인): `coordination/RELAYS/H-048-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-17.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `docs/OBSERVABILITY_FALLBACK_WARNING.md`
4. `coordination/TASK_BOARD.md`
5. `coordination/DECISIONS.md`
6. `coordination/HANDOFFS/H-049-fallback-warning-keep-frozen-resume-readiness-followup-check.md`
7. `coordination/REPORTS/H-048-result.md`, `coordination/REPORTS/H-048-review.md`, `coordination/RELAYS/H-048-review-to-main.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-17.md`
8. `coordination/REPORTS/H-036-result.md`, `coordination/REPORTS/H-037-result.md`, `coordination/REPORTS/H-038-result.md`, `coordination/REPORTS/H-039-result.md`, `coordination/REPORTS/H-042-result.md`, `coordination/REPORTS/H-043-result.md`, `coordination/REPORTS/H-044-result.md`, `coordination/REPORTS/H-045-result.md`, `coordination/REPORTS/H-046-result.md`, `coordination/REPORTS/H-047-result.md`

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
- `coordination/REPORTS/H-049-result.md`
- `coordination/RELAYS/H-049-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- `coordination/REPORTS/H-048-review.md`, `coordination/RELAYS/H-048-review-to-main.md`가 없으면 이번 라운드는 착수하면 안 된다. 먼저 H-048 review gate 완료 여부를 확인할 것.
- H-048 Executor 결과는 `INSUFFICIENT_SAMPLE_RATIO=0.7143`, `SUFFICIENT_DAYS=4`, 최근 3일 평균 전체 `parseEligibleRunCount=30.6667`, `requiredDistinctCompliantDays=3`까지 개선됐지만, 최종 Main 승인 전 상태라는 점을 결과 보고에 분리해 기록할 것.
- H-049에서는 H-048 최신 증거일(`2026-03-11` KST)과 다른 KST 날짜 창의 신규 증거를 우선 확보해 `SUFFICIENT_DAYS`/`INSUFFICIENT_SAMPLE_RATIO`/`requiredDistinctCompliantDays` 변화폭을 검증할 것.
- 진단/direct/chain 배치별 `SEED_TIMESTAMP` 분리 원칙을 유지하고, 같은 라운드 내 다중 날짜 실행이 불가능하다면 타임스탬프를 임의 조작하지 말고 실제 제약과 잔여 최소 distinct compliant day 수를 결과 보고에 남길 것.
- 최근 3일 평균 전체 모수(`parseEligibleRunCount=30.6667`)가 기준(`>=32`)에 매우 근접해 있으므로, 하루 실행 공백이 지표에 미치는 영향을 함께 분석할 것.
- fail-fast 체인 실패 원인 분류(`TEMPERATURE_UNSUPPORTED`, `MODEL_NOT_FOUND_OR_UNAVAILABLE`, `ALL_CANDIDATES_FAILED`, `OTHER`)를 동일 기준으로 집계해 이전 라운드 대비 재발 여부를 제시할 것.
- fallback-warning 용어는 output parsing fallback 경고만 의미한다. 모델 라우팅 fallback과 혼합 해석하지 않는다(`docs/OBSERVABILITY_FALLBACK_WARNING.md` 기준).
