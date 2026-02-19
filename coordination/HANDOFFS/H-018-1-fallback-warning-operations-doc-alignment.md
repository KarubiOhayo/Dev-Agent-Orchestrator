# H-018.1 fallback warning 운영 문서 산식/게이트 정합화

Owner: WT-18-1 (`codex/h018-1-fallback-warning-operations-doc-alignment`)
Priority: Highest

## 목표
- H-018 리뷰에서 보고된 P2/P3 이슈(진행률 산식 불일치, 착수 게이트 정의 혼재)를 해소한다.
- fallback warning 운영 지표의 산식/게이트 정의를 문서와 자동 점검 템플릿에서 단일 계약으로 고정한다.
- H-018 결과 보고서의 지표 표기와 근거 문구를 최신 계약과 정합화해 라운드 간 비교 가능성을 복원한다.

## 작업 범위
- 운영 문서 보정
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 라운드 결과 보고 정합화
  - `coordination/REPORTS/H-018-result.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 진행률 산식을 단일 기준으로 고정한다.
  - `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)` (표기 범위 0~100%)
  - 목표 초과 달성 정보가 필요하면 별도 지표(예: `목표 초과 일수`)로 분리 표기한다.
- 재보정 착수 게이트를 4개 기준으로 통일한다.
  - `집계 성공 >= 10`
  - `INSUFFICIENT_SAMPLE <= 0.50`
  - `집계 불가 < 3`
  - `샘플 충분 일수(parseEligibleRunCount >= 20) >= 7`
- `docs/code-agent-api.md`의 H-017/H-018 관련 섹션에서 3개/4개 게이트가 혼재된 문구를 제거하고 동일 집합으로 정리한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 진행률/게이트/판정 문구를 동일 기준으로 동기화한다.
- `coordination/REPORTS/H-018-result.md`의 진행률 수치/게이트 판정 문구를 최신 계약으로 보정한다.
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `INSUFFICIENT_SAMPLE` 제외 규칙(`parseEligibleRunCount < 20`)은 변경하지 않는다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`의 진행률 산식과 재보정 착수 게이트가 단일 기준으로 명시된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 출력 규칙이 동일 산식/게이트 기준으로 정렬된다.
3. `coordination/REPORTS/H-018-result.md`의 진행률/게이트 관련 수치 및 문구가 계약과 일치한다.
4. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경되지 않는다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 운영 문서 정합화 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-018-1-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-018-1-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 진행률 산식/게이트 정의 보정 전후 비교
  - H-018 결과 보고 정합화 내역(수치/문구)
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
