---
name: automation-nightly-test-report
description: "트리거: Automations에서 야간/정기 테스트 점검 결과를 inbox에 보고할 때 사용. 비트리거: 기능 구현, 파일 수정, 커밋/PR 생성이 목적일 때는 사용하지 않는다."
---

# automation-nightly-test-report

## 목적
- 정기 테스트 상태를 사람 개입 없이 점검하고 요약 보고한다.
- 실패 원인 후보를 빠르게 triage할 수 있는 최소 정보를 제공한다.

## 수행 절차
1. 테스트 명령을 실행한다.
   - 기본: `./gradlew clean test --no-daemon`
2. 성공/실패, 실패 테스트 목록, 최근 영향 범위를 요약한다.
3. 다음 액션 제안은 하되 코드/파일은 수정하지 않는다.

## 금지 사항 (Plan A)
- 파일 생성/수정/삭제 금지
- `git add/commit/push` 금지
- PR/브랜치 자동화 금지

## 표준 출력/파일 산출 규칙
- 출력은 inbox 보고용 텍스트로 작성한다.
  - 실행 시각(KST)
  - 실행 명령
  - 통과/실패
  - 실패 테스트 Top N
  - 권장 후속 조치(수동)
- 파일 산출 없음(필수)

