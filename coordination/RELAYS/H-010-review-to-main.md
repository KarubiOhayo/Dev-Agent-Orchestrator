# [H-010] Review -> Main Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-010-api-validation-error-contract.md`
- result: `coordination/REPORTS/H-010-result.md`
- review: `coordination/REPORTS/H-010-review.md`

## 리뷰 결과 요약
- 리스크 수준: `MEDIUM`
- P1 개수: `0`
- P2 개수: `1`
- P3 개수: `1`

## 핵심 Findings
1. [P2] API 문서의 400 오류 코드 매핑에 `INVALID_JSON_REQUEST`가 누락되어 구현(`ApiExceptionHandler`)과 계약이 불일치합니다.
2. [P3] Code 요청의 복합 필수조건 오류(`userRequest or specInputPath`)가 `details.field`에 단일 필드명이 아닌 문구로 노출되어 클라이언트 파싱 일관성 리스크가 있습니다.
3. 테스트 게이트는 Executor 보고 기준으로 통과(`./gradlew clean test --no-daemon`, `BUILD SUCCESSFUL`)했으며 공통 파일 변경 승인 절차 위반은 없습니다.

## 승인 게이트 체크
- 수용기준 충족 여부: **부분 충족**
  - 오류 응답 동작/테스트 게이트는 충족
  - 문서 오류 코드 매핑 정합성(수용기준 5) 보완 필요
- `./gradlew clean test --no-daemon` 통과 여부: 통과 (`BUILD SUCCESSFUL`, Executor 보고 인용)
- 공통 파일 변경 승인 절차 준수 여부: 준수 (`application.yml`, 공용 모델/빌드 설정 변경 없음)

## Main-Control 요청
- 권고 판단: `Conditional Go`
- 다음 라운드 제안 1건:
  - H-010 follow-up(소규모): 오류 코드 문서-구현 정합화(`INVALID_JSON_REQUEST`) 및 복합 필수조건 `details.field` 표현 규칙 명시/고정
