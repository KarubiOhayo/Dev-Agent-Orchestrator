# [H-018] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-018-fallback-warning-sample-plan-operations-check.md`
- main relay: `coordination/RELAYS/H-018-main-to-executor.md`
- result: `coordination/REPORTS/H-018-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 `H-018 운영 적용 점검 결과` 섹션 추가
    - 최근 14일 실측(게이트 4개 + agent별 모수)
    - H-017 목표 대비 진행률/미달률
    - Projection 오차(`deltaSufficientDays`, `deltaInsufficientRatio`, `deltaStartDate`)
    - 재보정 보류 판정/근거 + 원인 분류/우선순위 액션
    - 임계치/알림 룰 수치 및 `INSUFFICIENT_SAMPLE` 제외 규칙 유지 재확인
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 H-018 기준으로 갱신
    - Projection 오차 3종 출력 항목 추가
    - 오차 허용 기준 초과 시 자동 `보정 보류 + 원인 분류 + 우선순위 액션` 출력 규칙 추가
    - 게이트 충족 시 “임계치 후보 산정 라운드 제안” 문구로 정리
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-018-result.md`
  - `coordination/RELAYS/H-018-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md` H-018 섹션의 수치 일관성 검증
   - 최근 14일 실측값/목표 대비 진행률/Projection 오차 수치가 상호 정합적인지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 출력 규칙 검증
   - `deltaSufficientDays`, `deltaInsufficientRatio`, `deltaStartDate` 계산/출력 요구가 누락 없이 반영됐는지
   - 오차 초과 시 자동 보류 규칙, 착수 가능 시 다음 라운드 제안 문구가 정확한지
3. 유지 원칙 불변 검증
   - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙, 이벤트/모수 정의가 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 최근 14일 샘플 충분 일수(`>=20`)가 0일이라 재보정 착수 조건 충족까지 추가 모수 확보가 필요함
- `DOC`/`REVIEW` 체인 모수 부족으로 agent 간 균형 추세 해석 신뢰도 제한이 지속됨

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-018-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
