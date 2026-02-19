# H-014.1 Code `parseEligibleRunCount` 모수 정의 정합화

Owner: WT-14-1 (`codex/h014-1-code-parse-eligible-alignment`)
Priority: Highest

## 목표
- H-014 리뷰에서 보고된 P2 1건(모수 정의 충돌)을 해소한다.
- Code `parseEligibleRunCount` 정의를 실제 실행 경로(직접 호출 + Spec 체인 호출)와 일치시킨다.
- fallback warning 운영 문서/점검 템플릿 간 해석 기준을 단일화한다.

## 작업 범위
- 운영 문서 보정
  - `docs/code-agent-api.md`
- 자동 점검 템플릿 정합화(필요 시)
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 정합화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- `docs/code-agent-api.md`의 fallback warning 집계 기준 섹션에서 Code 모수 정의를 아래 기준으로 고정한다.
  - Code: 직접 `POST /api/agents/code/generate` 호출 run + Spec 체인(`chainToCode=true`)으로 내부 Code 서비스가 실행된 run 포함
  - Spec/Doc/Review도 "agent 서비스 run(직접 호출 + 체인 실행 포함)" 원칙으로 동일 표기
- 문서 내 동일 섹션에서 상충되는 문구(직접 호출만 포함으로 읽히는 표현)를 제거한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 Code 모수 예시/설명이 직접 호출 기준으로 축소 해석될 여지가 있으면 함께 정정한다.
- 임계치 값(`0.05`, `0.15`)과 `INSUFFICIENT_SAMPLE` 제외 규칙 자체는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`의 Code `parseEligibleRunCount`가 직접 호출 + Spec 체인 호출 포함 기준으로 명시된다.
2. `parseEligibleRunCount` 집계 단위 설명에서 agent별 문구 충돌이 제거된다.
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`와 문서 해석 기준이 일치한다(해당 항목이 존재할 때).
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- run-state 이벤트 추가/스키마 변경
- 임계치/알림 룰 수치 조정
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 운영 문서 정합화 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-014-1-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-014-1-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - Code 모수 정의 보정 전/후 비교
  - 템플릿 정합화 여부(변경 없음이면 "없음" 명시)
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
