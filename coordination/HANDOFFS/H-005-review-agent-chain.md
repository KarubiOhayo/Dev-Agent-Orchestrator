# H-005 ReviewAgent + Code -> Review Chain

Owner: WT-5 (`codex/review-chain`)
Priority: High

## 목표
- ReviewAgent를 구현하고 Code 결과를 입력으로 리뷰 생성 체이닝을 연결한다.

## 작업 범위
- `src/main/java/me/karubidev/devagent/agents/review/` 신규 추가
  - `ReviewAgentService`
  - `ReviewGenerateRequest`, `ReviewGenerateResponse`
  - 필요 시 `ReviewOutputSchemaParser`(JSON 우선 + fallback 경고 이벤트)
- Review API endpoint 추가 (`/api/agents/review/generate`)
- Code -> Review chain service 추가(최소 1개 호출 경로)
  - `CodeGenerateRequest`: `chainToReview`, `reviewUserRequest` 추가
  - `CodeGenerateResponse`: `chainedReviewResult` 추가
  - `CodeAgentService`: Review chain 실행 연결
- run-state 이벤트 추가
  - `CHAIN_REVIEW_TRIGGERED`, `CHAIN_REVIEW_DONE`, `CHAIN_REVIEW_FAILED`
  - (fallback 도입 시) `REVIEW_OUTPUT_FALLBACK_WARNING`
- 테스트 추가
  - ReviewAgent 단위 테스트
  - Code -> Review 체인 테스트
  - 기존 Code 테스트 회귀 없음 확인
- 문서 업데이트
  - `docs/code-agent-api.md`에 `chainToReview` 요청/응답 계약 반영

## 수용 기준
1. `POST /api/agents/review/generate`가 구조화된 리뷰 JSON 응답을 반환한다.
2. `chainToReview=true`일 때 Code 완료 후 ReviewAgent 체이닝이 동작한다.
3. 체인 호출 결과가 run-state 이벤트로 기록된다.
4. `./gradlew clean test` 통과.

## 비범위
- 리뷰 결과 기반 자동 코드 수정(autofix)
- PR 자동 생성/외부 채널 연동
- 부분 성공 정책(H-007) 확정

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 메인 컨트롤 사전 승인 필수.
- JSON 우선 원칙 유지, fallback 사용 시 경고 이벤트 기록.
- 경로/파일 쓰기 안전성 정책 기존 수준 유지.

## 보고서
- 완료 시 `coordination/REPORTS/H-005-result.md` 갱신
- 리뷰 스레드 결과는 `coordination/REPORTS/H-005-review.md` 작성
- 필수 항목:
  - 변경 파일 목록
  - 테스트 결과(명령 + 통과/실패)
  - API 요청/응답 예시
  - 남은 리스크
  - 공통 파일 변경 승인 여부
