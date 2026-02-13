# H-008.1 심볼릭 링크 경계 우회 차단 보강

Owner: WT-8 (`codex/apply-boundary-symlink-fix`)
Priority: Highest

## 목표
- `FileApplyService`의 경계 검증에서 심볼릭 링크 경유 우회를 차단한다.
- `targetProjectRoot` 바깥으로의 실제 파일 쓰기를 방지하는 회귀 테스트를 추가한다.
- H-008 리뷰 P2를 해소해 승인 게이트를 `Go`로 전환한다.

## 작업 범위
- 코드 보강
  - `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java`
  - 필요 시 경계 검증 유틸/헬퍼 추가(동일 패키지 한정)
- 테스트 보강
  - `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java`

## 구현 지침
- 기존 `startsWith(normalizedRoot)` 기반 사전 검증에 더해,
  실제 쓰기 직전 경로를 `NOFOLLOW_LINKS` 정책 기준으로 재검증한다.
- 심볼릭 링크가 포함된 경로(`targetRoot/link-outside/file`)가 root 밖을 가리키면
  쓰기를 수행하지 않고 `REJECTED`로 처리한다.
- 경계 위반과 I/O 오류(`ERROR`) 상태 계약을 혼합하지 않는다.

## 수용 기준
1. 심볼릭 링크 경유 우회 케이스에서 root 바깥 쓰기가 차단된다.
2. 우회 차단 시 `REJECTED` 상태와 메시지가 테스트로 고정된다.
3. 기존 경계 케이스(빈 경로/절대경로/`..`/invalid path) 회귀가 없다.
4. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 체인 실패 전파 정책(API 계약) 변경(H-009)
- API 입력검증/에러계약 전면 표준화(H-010)
- 프롬프트 자산 보강(H-011)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.

## 보고서
- 완료 시 `coordination/REPORTS/H-008-1-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-008-1-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 심볼릭 링크 우회 케이스 재현/차단 증빙
  - 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
