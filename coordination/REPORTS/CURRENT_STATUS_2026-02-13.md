# Current Status Report (2026-02-13)

## 요약
Spec -> Code -> Doc/Review 체이닝(1차), Code 출력 JSON files[] 우선화, CLI 고도화(H-006: `--json`, alias, 반복 실행 성능)까지 완료됨.
- H-006 최종 정합성 확인 결과: 리뷰 `No findings`, `./gradlew clean test --no-daemon` 통과, Go 판단 가능
- H-007 최종 정합성 확인 결과: 수용기준 충족, 리뷰 `P1/P2=0`, `./gradlew clean test --no-daemon` 통과, Go 확정
- H-008 최종 정합성 확인 결과: `Conditional Go` (P2: 심볼릭 링크 경계 우회 가능성)
- H-008.1 보강 결과: 리뷰 `No findings`, `./gradlew clean test --no-daemon` 통과, Go 확정
- H-009 최종 정합성 확인 결과: 리뷰 `No findings`, `./gradlew clean test --no-daemon` 통과, Go 확정
- 라우팅 -> LLM 호출 -> JSON 우선 파싱 -> dry-run/apply -> run-state 기록
- 운영 체계는 3스레드(Main/Review/Executor) + stateless 라운드 방식으로 전환

## 구현 완료 항목
- ModelRouter + Routing API
- Provider adapters(OpenAI/Anthropic/Google)
- CodeAgent API
- SpecAgent API + Spec -> Code chain
- PromptRegistry + ContextPolicy
- RunState(SQLite/fallback)
- apply/dry-run + path safety(spec in/out)
- Code output JSON files[] 우선 파싱 + markdown fallback warning 이벤트
- DocAgent API + 문서 JSON 스키마 파싱/보정 + fallback warning 이벤트
- Code -> Doc chain (`chainToDoc`, `docUserRequest`) + chain 이벤트 기록
- ReviewAgent API + 리뷰 JSON 스키마 파싱/보정 + fallback warning 이벤트
- Code -> Review chain (`chainToReview`, `reviewUserRequest`) + chain 이벤트 기록
- CLI 엔트리포인트(`devagent generate/spec/help`) + 결과 표/리스트 formatter
- CLI 오류 메시지 간소화(스택트레이스 기본 노출 차단) + API docs `files` 계약 반영
- CLI 실행 래퍼 `bootJar -> java -jar` 전환으로 종료코드 정확 전달(`help=0`, unknown option=`2`)
- CLI `--json` 출력(성공/실패 envelope), 대표 alias, 반복 실행 성능 개선(H-006)
- strict-json 라우팅 정책 재설계(H-007): tri-state 입력 + 명시 요청 기반 escalation + 우선순위 회귀 테스트 고정
- 파일 적용 경계 하드닝(H-008) + 심볼릭 링크 우회 차단(H-008.1)
- 체인 실패 전파 정책 확정(H-009): `FAIL_FAST` 기본 + `PARTIAL_SUCCESS` 선택, `chainFailures[]` 응답 계약 도입
- 테스트 추가 및 통과

## 잔여 과제
1. API 입력검증/에러계약 표준화(H-010)
2. spec/doc/review 프롬프트 자산 보강(H-011)

## 리스크
- 모델 출력이 비정형일 때 markdown fallback 빈도가 높아질 수 있음
- JSON 구조는 맞아도 files[]/document/review 항목 의미 검증은 아직 제한적
- `devagent` 스크립트의 빌드 산출물 신선도 판단이 mtime 기반이므로 특수 케이스에서 오판 가능성 존재
- `PARTIAL_SUCCESS` 사용 시 클라이언트가 `chainFailures`를 확인하지 않으면 체인 실패를 간과할 수 있음
- 체인 실패 단계는 `CHAIN_DOC`/`CHAIN_REVIEW` 수준으로만 제공되어 세부 원인 분류는 제한적

## 메인 제안
다음 라운드는 H-010(API 입력검증/에러계약 표준화)을 handoff로 고정해 진행
