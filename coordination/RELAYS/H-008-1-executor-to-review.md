# [H-008.1] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-008-1-symlink-boundary-fix.md`
- result: `coordination/REPORTS/H-008-1-result.md`

## 구현 요약
- `FileApplyService`에 실제 경로 기반 경계 재검증(`toRealPath`)을 추가해 심볼릭 링크 경유 우회를 차단했습니다.
- 경계 재검증은 쓰기 직전 단계(디렉터리 생성 전/후)에 수행되며, 우회 시 `REJECTED` + `path traversal is not allowed`로 고정됩니다.
- 경계 위반(`REJECTED`)과 I/O 실패(`ERROR`) 상태 계약은 분리 유지했습니다.

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java`
- `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java`

## 재현-차단 증빙(테스트)
- 신규 테스트: `rejectSymlinkBoundaryBypass`
  - `tempDir/link-outside -> outsideDir` 심볼릭 링크 생성
  - 입력: `link-outside/evil.txt`
  - 검증:
    - `REJECTED`
    - `path traversal is not allowed`
    - `outsideDir/evil.txt` 미생성

## 테스트 게이트
- 대상 테스트:
  - `./gradlew test --no-daemon --tests me.karubidev.devagent.agents.code.apply.FileApplyServiceTest`
  - 결과: **BUILD SUCCESSFUL**
- 전체 게이트:
  - `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 리뷰 집중 포인트
1. 실제 경로 환산 로직(`findNearestExistingAncestor` + `toRealPath`)이 심볼릭 링크 우회를 충분히 차단하는지
2. 우회 차단 시 상태/메시지 계약(`REJECTED`, `path traversal is not allowed`) 일관성
3. `ERROR` 상태가 I/O 예외 케이스에만 유지되는지(경계 위반과 혼합 여부)

## 남은 리스크 / 오픈 이슈
- 심볼릭 링크 미지원 환경에서는 신규 테스트가 skip 될 수 있음

## 승인 필요 항목
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 없음
