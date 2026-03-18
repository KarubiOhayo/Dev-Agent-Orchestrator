# Parking Lot

Last Updated: 2026-03-18

## fallback-warning observability track
- item name: `fallback-warning keep-frozen / resume-readiness track`
- current status: `PARKED_UNLESS_EXPLICIT_RESUME`
- why parked: 반복 follow-up이 active roadmap를 잠식하고 있었고, 현재 더 큰 가치는 portfolio readiness 정리에 있다. fallback-warning은 지금 제품/portfolio readiness의 직접 blocker가 아니다.
- what remains unresolved: historical ledger 기준으로 `INSUFFICIENT_SAMPLE_RATIO <= 0.50`, `SUFFICIENT_DAYS >= 7` 같은 재개 게이트는 아직 닫히지 않았다. H-024 거버넌스 고정과 H-049 follow-up은 보존만 하고 자동 확장하지 않는다.
- allowed resume triggers:
  1. 사용자가 fallback-warning 재개를 명시적으로 요청한 경우
  2. parser fallback-warning 관련 실제 회귀/incident가 발생한 경우
  3. release/demo/portfolio 공개를 막는 직접 blocker로 확인된 경우
- resume entrypoint:
  - 먼저 `coordination/REPORTS/H-048-result.md`, `coordination/REPORTS/H-048-review.md`, `coordination/RELAYS/H-048-review-to-main.md`를 다시 읽는다.
  - 그다음 `coordination/HANDOFFS/H-049-fallback-warning-keep-frozen-resume-readiness-followup-check.md`, `coordination/HANDOFFS/H-024-fallback-warning-recovery-action-baseline-governance.md`를 historical context로만 참고한다.
  - 실제 재개는 stale handoff/relay를 그대로 잇지 않고 fresh handoff를 새로 만들어 시작한다.
- last known references:
  - `coordination/REPORTS/H-048-result.md`
  - `coordination/REPORTS/H-048-review.md`
  - `coordination/RELAYS/H-048-review-to-main.md`
  - `coordination/HANDOFFS/H-049-fallback-warning-keep-frozen-resume-readiness-followup-check.md`
  - `coordination/RELAYS/H-049-main-to-executor.md`
  - `coordination/HANDOFFS/H-024-fallback-warning-recovery-action-baseline-governance.md`
  - `coordination/RELAYS/H-024-main-to-executor.md`
  - `docs/OBSERVABILITY_FALLBACK_WARNING.md`
  - `coordination/DECISIONS.md`
