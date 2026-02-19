# H-014.1 결과 보고서 (Code `parseEligibleRunCount` 모수 정의 정합화)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `docs/code-agent-api.md`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`

## 구현 요약
- `docs/code-agent-api.md`의 fallback warning 집계 기준에서 `parseEligibleRunCount` 해석 충돌을 제거했습니다.
  - 공통 원칙으로 "직접 API 호출 run + 체인 호출로 내부 서비스가 실행된 run 포함"을 유지했습니다.
  - Code 모수를 "직접 `POST /api/agents/code/generate` 호출 run + Spec `chainToCode=true` 체인으로 내부 Code 서비스가 실행된 run"으로 명시했습니다.
  - Spec 항목은 현재 체인 유입 경로가 없음을 명시해 해석 오차를 줄였습니다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 동일한 `parseEligibleRunCount` 기준을 추가했습니다.
  - Code/Spec/Doc/Review별 모수 포함 범위를 문서와 동일하게 정렬했습니다.
- 임계치 값(`0.05`, `0.15`)과 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않았습니다.

## Code 모수 정의 보정 전/후 비교
- 보정 전:
  - `docs/code-agent-api.md`에서 Code 모수가 "직접 `POST /api/agents/code/generate` 호출 run"으로만 읽혀 Spec 체인 실행분이 누락 해석될 수 있었습니다.
- 보정 후:
  - Code 모수를 "직접 호출 run + Spec `chainToCode=true` 체인으로 내부 Code 서비스가 실행된 run"으로 명시했습니다.
  - "다른 agent 흐름에서 내부 실행된 서비스 run도 해당 agent 모수에 포함" 원칙을 추가해 agent 간 문구 충돌을 제거했습니다.

## 템플릿 정합화 여부
- 변경 있음:
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `parseEligibleRunCount` 해석 기준 및 agent별 포함 범위를 추가해 `docs/code-agent-api.md`와 정합화했습니다.

## 수용기준 점검
1. `docs/code-agent-api.md` Code 모수에 직접 호출 + Spec 체인 포함 기준 명시: **충족**
2. `parseEligibleRunCount` 집계 단위 설명의 agent별 문구 충돌 제거: **충족**
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 해석 기준 정합화: **충족**
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경 없음: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- fallback warning 임계치/알림 룰은 운영 초기 기준이므로 실측 데이터 누적 후 보정 필요성은 남아 있습니다.
- Spec 체인 외 추가 체인 경로가 도입될 경우 `parseEligibleRunCount` 기준 문구를 동일 원칙으로 재검토해야 합니다.

## 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 추가 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 Code 모수 정의가 Spec 체인 실행 포함 기준으로 충분히 고정됐는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 agent별 모수 정의가 문서 기준과 동일한지
3. 임계치/알림 룰 수치(`0.05`, `0.15`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
