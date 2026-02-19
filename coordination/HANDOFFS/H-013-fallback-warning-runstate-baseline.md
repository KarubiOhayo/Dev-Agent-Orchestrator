# H-013 fallback warning run-state 집계 기준 문서화

Owner: WT-13 (`codex/h013-fallback-warning-baseline`)
Priority: Highest

## 목표
- fallback warning 이벤트(`CODE/SPEC/DOC/REVIEW`)의 운영 집계 기준을 단일 문서 규약으로 고정한다.
- 경고율 산식, 임계치, 알림 룰을 명시해 운영 판단이 담당자별 해석에 의존하지 않도록 한다.
- 자동 점검 템플릿(야간 보고)에 동일 기준을 반영해 보고 형식 일관성을 확보한다.

## 작업 범위
- 운영 문서 보강
  - `docs/code-agent-api.md`
  - `docs/PROJECT_OVERVIEW.md`
- 자동 점검 템플릿 동기화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`

## 구현 지침
- `docs/code-agent-api.md`에 다음 항목을 명시한다.
  - 대상 이벤트 목록: `CODE_OUTPUT_FALLBACK_WARNING`, `SPEC_OUTPUT_FALLBACK_WARNING`, `DOC_OUTPUT_FALLBACK_WARNING`, `REVIEW_OUTPUT_FALLBACK_WARNING`
  - 집계 단위: agent별 일 단위 집계 + 전체 집계
  - 경고율 산식(문서 계약):
    - `warningRate = warningEventCount / parseEligibleRunCount`
    - `parseEligibleRunCount` 정의와 최소 샘플 수 조건을 명시
  - 임계치(예: 정상/주의/경고)와 알림 룰(연속 초과/급증 조건) 표를 명시
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에는 fallback warning 점검 섹션을 추가한다.
  - 필수 출력: 집계 기간, 이벤트별 건수, agent별 경고율, 임계치 판정, 후속조치 권고
  - Plan A 제약(파일 수정/커밋 금지)은 유지한다.
- `docs/PROJECT_OVERVIEW.md`에는 운영 리스크/다음 우선순위를 H-013 결과에 맞춰 최소 반영한다.

## 수용 기준
1. `docs/code-agent-api.md`에 fallback warning 집계 기준(이벤트/모수/경고율/임계치/알림 룰)이 반영된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 기준으로 점검 보고를 요구하도록 갱신된다.
3. `docs/PROJECT_OVERVIEW.md`의 운영 리스크/우선순위가 갱신 내용과 정합한다.
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 API/엔드포인트/이벤트 추가
- run-state 저장 스키마 변경
- 자동 알림 전송 구현(웹훅/PR/커밋 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 본 라운드는 운영 기준 문서화 범위이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-013-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-013-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 집계 기준(모수/경고율/임계치/알림 룰) 요약
  - 자동 점검 템플릿 반영 내용
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
