# H-018 리뷰 보고서

## 대상
- handoff: `coordination/HANDOFFS/H-018-fallback-warning-sample-plan-operations-check.md`
- result: `coordination/REPORTS/H-018-result.md`
- relay: `coordination/RELAYS/H-018-executor-to-review.md`

## Findings (P1 > P2 > P3)

### [P2] 목표 대비 진행률 산식과 보고 수치가 불일치함
- 근거 파일/라인:
  - `docs/code-agent-api.md:261`
  - `docs/code-agent-api.md:323`
  - `coordination/REPORTS/H-018-result.md:48`
- 영향:
  - H-017에서 정의한 `집계 성공 진행률 = min(1, 집계성공일수 / 10)` 산식 기준이면 14일은 `100%`가 되어야 하지만, H-018 보고는 `140%`로 기재되어 동일 지표의 라운드 간 비교 가능성이 저하됩니다.
- 권고 수정:
  - (안1) H-017 산식을 유지해 H-018 진행률 값을 `100%`로 보정하거나
  - (안2) 상한 없는 산식으로 운영하려면 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 산식 설명을 동일 기준으로 함께 개정해 정합성을 고정합니다.

### [P3] 재보정 착수 게이트 정의가 문서 내에서 3개/4개로 혼재됨
- 근거 파일/라인:
  - `docs/code-agent-api.md:275`
  - `docs/code-agent-api.md:351`
  - `coordination/REPORTS/H-018-result.md:68`
- 영향:
  - H-017 분기 규칙은 3개 게이트(집계 성공/샘플 부족 비율/집계 불가) 중심으로 서술되어 있고, H-018 판정은 4개 게이트(샘플 충분 일수 포함)로 집계되어 운영자가 문서 어느 부분을 기준으로 판단해야 하는지 모호해집니다.
- 권고 수정:
  - `docs/code-agent-api.md`의 H-017 분기 규칙과 H-018 판정 문구를 동일 게이트 집합(샘플 충분 일수 포함 여부)으로 통일하고, 야간 템플릿 문구도 같은 기준으로 맞춥니다.

## 검증 근거 (파일/라인)
1. H-018 운영 적용 점검 산출물 반영 확인
- `docs/code-agent-api.md:285`
- `docs/code-agent-api.md:297`
- `docs/code-agent-api.md:328`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:42`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:58`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:65`

2. 임계치/알림 룰 수치 및 제외 규칙 유지 확인
- `docs/code-agent-api.md:368`
- `docs/code-agent-api.md:369`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:68`
- `coordination/REPORTS/H-018-result.md:86`

3. 테스트 게이트/공통 파일 변경 여부(Executor 보고 인용)
- `coordination/REPORTS/H-018-result.md:91`
- `coordination/REPORTS/H-018-result.md:92`
- `coordination/REPORTS/H-018-result.md:100`

## 심각도 집계
- P1: 0
- P2: 1
- P3: 1

## 수용기준 검증
1. `docs/code-agent-api.md`에 H-018 실측/진행률/Projection 오차/보류 판정 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 오차/원인 분류/다음 액션 규칙 반영: **충족**
3. 게이트 미충족 보류 근거와 보완 액션 수치 기반 보고: **충족**
4. 임계치/알림 룰 수치와 `INSUFFICIENT_SAMPLE` 제외 규칙 유지: **충족**
5. 코드/설정 변경 없음: **충족** (Executor 보고 인용)
6. `./gradlew clean test --no-daemon` 통과: **충족** (`BUILD SUCCESSFUL`, Executor 보고 인용)

## 승인 게이트 체크
- 테스트 게이트 상태(Executor 보고 인용):
  - `./gradlew clean test --no-daemon` -> `BUILD SUCCESSFUL`
  - 근거: `coordination/REPORTS/H-018-result.md:91`, `coordination/REPORTS/H-018-result.md:92`
- Review-Control 제약상 테스트 재실행은 수행하지 않았고, 결과 리포트/릴레이/변경 문서를 대조 검증함.
- 공통 파일 변경 승인 절차 준수 여부:
  - 공통 승인 대상(`application.yml`, 공용 모델/계약, 빌드 설정) 변경 없음
  - 근거: `coordination/REPORTS/H-018-result.md:100`

## 리뷰 결론
- 리스크 수준: `MEDIUM`
- 최종 권고: `Conditional Go` (문서 산식/게이트 정의 정합성 보완 조건)
