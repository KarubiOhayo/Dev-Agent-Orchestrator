# H-014 fallback warning 집계 기준 문구 정합화

Owner: WT-14 (`codex/h014-fallback-warning-baseline-alignment`)
Priority: Highest

## 목표
- H-013 리뷰에서 보고된 문구 정합성 이슈(P2/P3) 2건을 해소한다.
- `parseEligibleRunCount`를 API 엔드포인트 호출 수가 아닌 "agent 서비스 run 기준(직접 호출 + 체인 호출 포함)"으로 고정한다.
- `INSUFFICIENT_SAMPLE`(`parseEligibleRunCount < 20`)의 임계치/알림 계산 제외 규칙을 야간 점검 템플릿에 명시한다.

## 작업 범위
- 운영 문서 보정
  - `docs/code-agent-api.md`
- 자동 점검 템플릿 동기화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 정합화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- `docs/code-agent-api.md`의 fallback warning 집계 기준 섹션에서 `parseEligibleRunCount` 정의를 명확히 보정한다.
  - `CODE/SPEC`: 직접 API 호출 run 기준
  - `DOC/REVIEW`: 직접 API 호출 + Code 체인 호출 run 모두 포함
  - 해석 기준은 "agent 서비스 run 단위"로 통일
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 fallback warning 점검 절차/출력 형식에 아래를 명시한다.
  - `parseEligibleRunCount < 20`은 `INSUFFICIENT_SAMPLE`
  - `INSUFFICIENT_SAMPLE` 대상은 임계치 판정과 알림 룰 계산에서 제외
  - 보고서에는 제외 사실과 사유를 별도 표기
- 문구 보정은 운영 계약 정합화 범위로 제한하고, 임계치 값 자체(`0.05`, `0.15`)와 알림 조건 정의는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 `parseEligibleRunCount`의 직접 호출/체인 호출 포함 기준이 agent별로 명시된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `INSUFFICIENT_SAMPLE` 임계치/알림 계산 제외 규칙이 명시된다.
3. H-013 리뷰 보고서의 P2/P3 지적 사항이 문서 기준으로 해소된다.
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 fallback 이벤트/run-state 스키마 추가
- 임계치/알림 룰 수치 조정
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 본 라운드는 문구 정합성 보정 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-014-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-014-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - `parseEligibleRunCount` 정의 보정 전/후 요약
  - `INSUFFICIENT_SAMPLE` 제외 규칙 반영 위치(파일/섹션)와 보고 예시
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
