# H-018.1 결과 보고서 (fallback warning 운영 문서 산식/게이트 정합화)

## 상태
- 현재 상태: **완료 (문서 산식/게이트 정합화 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- `coordination/REPORTS/H-018-result.md`
- `coordination/REPORTS/H-018-1-result.md`
- `coordination/RELAYS/H-018-1-executor-to-review.md`

## 구현 요약
- `docs/code-agent-api.md`의 H-017/H-018 구간을 단일 계약으로 정렬했습니다.
  - `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)`를 `0~100%` 표기로 고정했습니다.
  - 목표 초과 정보는 `목표 초과 일수 = max(0, 집계 성공 일수 - 10)`로 분리 표기하도록 명시했습니다.
  - 재보정 착수/보류 분기에서 게이트를 4개(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)로 통일했습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화했습니다.
  - H-017 진행률 산식과 목표 초과 일수 분리 표기 규칙을 명시했습니다.
  - 재보정 판정 문구를 4개 게이트 기준으로 고정했습니다.
- `coordination/REPORTS/H-018-result.md`를 최신 계약으로 보정했습니다.
  - `집계 성공 달성률`을 `140% -> 100%`로 수정했습니다.
  - `집계 성공 목표 초과 일수 = 4일`을 별도 표기했습니다.
  - 재보정 보류 근거에 4개 게이트 집합을 명시했습니다.

## 진행률 산식/게이트 정의 보정 전후 비교

| 구분 | 보정 전 | 보정 후 |
|---|---|---|
| 집계 성공 달성률 표기 | 문서 산식은 상한(`min`) 기준이 있었지만, H-018 보고에서 `140%` 표기 | `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)`로 고정 (`0~100%`) |
| 목표 초과 정보 | 진행률 표기에 초과분이 혼합됨 | `목표 초과 일수`를 별도 지표로 분리 |
| 재보정 착수 게이트 | H-017 분기 문구는 3개, H-018 판정은 4개로 혼재 | 4개 게이트(`집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수 >= 7`)로 통일 |
| 보류 판정 문구 | "위 3개 게이트" 표현 존재 | "위 4개 게이트"로 정합화 |

## H-018 결과 보고 정합화 내역 (수치/문구)
- 수치 보정:
  - `집계 성공 달성률`: `140% -> 100%`
- 문구 보정:
  - `집계 성공 목표 초과 일수 = 4일` 추가
  - 재보정 보류 근거에 4개 게이트 전체 집합을 명시하고 미충족 항목(`INSUFFICIENT_SAMPLE`, `샘플 충분 일수`)을 분리 표기

## 수용기준 점검
1. `docs/code-agent-api.md` 진행률 산식/재보정 게이트 단일 기준 명시: **충족**
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 출력 규칙 동일 기준 정렬: **충족**
3. `coordination/REPORTS/H-018-result.md` 진행률/게이트 수치 및 문구 정합화: **충족**
4. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙 유지: **충족**
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경 없음: **충족**
6. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 최근 14일 실측에서 샘플 충분 일수(`parseEligibleRunCount >= 20`)가 0일이라 재보정 착수 지연 리스크는 지속됩니다.
- `DOC`/`REVIEW` 모수 부족이 이어지면 agent 간 비교 지표 신뢰도가 낮게 유지될 수 있습니다.

## 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약 파일, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 진행률 산식/4개 게이트 계약이 동일한지
2. `coordination/REPORTS/H-018-result.md`의 달성률 `100%` 및 `목표 초과 일수` 분리 표기가 일치하는지
3. 임계치/알림 룰 수치 및 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
