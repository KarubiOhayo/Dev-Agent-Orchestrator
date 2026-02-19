# [H-018-1] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-018-1-fallback-warning-operations-doc-alignment.md`
- main relay: `coordination/RELAYS/H-018-1-main-to-executor.md`
- result: `coordination/REPORTS/H-018-1-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에서 진행률/게이트 계약을 단일 기준으로 고정
    - `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)` (`0~100%`)
    - `목표 초과 일수` 분리 표기
    - 재보정 착수/보류 게이트 4개 기준 통일
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 동일 산식/게이트/판정 문구 동기화
  - `coordination/REPORTS/H-018-result.md`에서 `집계 성공 달성률 140% -> 100%` 보정 및 `목표 초과 일수` 추가
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-018-result.md`
  - `coordination/REPORTS/H-018-1-result.md`
  - `coordination/RELAYS/H-018-1-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. H-017/H-018 관련 문서(`docs/code-agent-api.md`)에서 3개/4개 게이트 혼재 문구가 제거되고 4개 게이트 기준으로 통일됐는지
2. 자동화 템플릿(`coordination/AUTOMATIONS/A-001-nightly-test-report.md`)에 진행률 산식(`min`) + 목표 초과 일수 분리 + 4개 게이트 판정 문구가 일관되게 반영됐는지
3. H-018 결과 보고(`coordination/REPORTS/H-018-result.md`)의 수치/문구 보정(`100%`, 목표 초과 일수, 4개 게이트 근거)이 계약과 일치하는지

## 알려진 리스크 / 오픈 이슈
- 샘플 충분 일수 부족(14일 기준 0일)으로 재보정 착수 판단 지연 리스크는 여전히 존재

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-018-1-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
