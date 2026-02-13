# H-008 Review Report

## 대상
- handoff: `coordination/HANDOFFS/H-008-apply-boundary-hardening.md`
- result: `coordination/REPORTS/H-008-result.md`
- relay: `coordination/RELAYS/H-008-executor-to-review.md`

## Findings (P1 > P2 > P3)
### P2
1. 심볼릭 링크 경유 시 target root 바깥으로 쓰기가 가능한 경계 우회가 남아 있습니다.
   - 근거:
     - `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java:76` 에서 `startsWith(normalizedRoot)`는 문자열/경로 prefix 기준 검증입니다.
     - `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java:99` 의 실제 쓰기 시점은 심볼릭 링크를 따라갈 수 있어, `targetRoot/link-outside/file` 형태가 root 내부로 판정되어도 외부 경로에 기록될 수 있습니다.
     - `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java:40` 이후 테스트는 절대경로/`..`/invalid path는 다루지만 심볼릭 링크 경계 우회 케이스는 없습니다.
   - 영향: H-008의 핵심 목표인 파일 적용 경계 방어 관점에서 우회 가능성이 남아 의도치 않은 외부 파일 쓰기를 완전히 차단하지 못합니다.

## 수용기준 검증
1. 파일 적용 경계 위반 입력(절대경로/경로탈출/빈 경로) 차단: **충족**
   - `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java:40`
2. 경계 위반 시 상태/메시지 고정: **충족**
   - `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java:48`
3. `targetProjectRoot`/`specInputPath` 실패 run-state 계약 유지: **충족**
   - `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java:225`
4. `ModelRouter.resolve` 실패 경로 회귀 테스트 추가: **충족**
   - `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java:17`
5. 테스트 게이트 통과: **충족**
   - 실행: `./gradlew clean test --no-daemon`
   - 결과: `BUILD SUCCESSFUL` (Review thread 재실행 확인)

## 승인 게이트 점검
- P1: `0`
- P2: `1`
- P3: `0`
- 공통 파일 변경 승인 절차:
  - `src/main/resources/application.yml` 변경 없음
  - 공용 모델/빌드 설정 변경 없음
  - 사전 승인 절차 위반 없음

## 리뷰 결론
- 리스크 수준: `MEDIUM`
- 최종 판단: `Conditional Go` (P2 경계 우회 보강 필요)
