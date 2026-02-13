# H-007 결과 보고서 (라우팅 strict-json 정책 재설계)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일 목록
- `src/main/java/me/karubidev/devagent/orchestration/routing/RouteRequest.java`
- `src/main/java/me/karubidev/devagent/orchestration/routing/ModelRouter.java`
- `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java`

## strict-json/우선순위 정책 diff 요약
- `RouteRequest.strictJsonRequired`를 `Boolean` tri-state로 변경해 기본값을 미지정(`null`)으로 전환.
- strict-json escalation 조건을 `strictJsonRequired == true`인 경우로 제한(미지정/null/false는 미적용).
- `ModelRouter` escalation 순서를 `review-high-risk -> strict-json -> large-context`로 고정.
- `LinkedHashSet` 기반 후보 중복 제거 시 우선순위가 유지되도록 동시 조건 회귀 테스트로 고정.

## 수용기준 충족 여부
1. strict-json 미지정 기본 요청에서 모드 기본 primary 선택: **충족**
   - `routeCodeBalancedWithoutStrictJsonEscalationUsesCodexByDefault`
2. strict-json 명시 요청에서 정책 우선순위 반영: **충족**
   - `routeCodeBalancedWithStrictJsonEscalatesToStrictJsonModel`
3. `review-high-risk` + `large-context` + `strict-json` 동시 조건 순서 고정: **충족**
   - `reviewHighRiskStrictJsonAndLargeContextPreservesEscalationPriorityOrder`
4. 기존 라우팅 동작 회귀 없음(`COST_SAVER`, canary fallback): **충족**
   - `routeRouterCostSaverUsesFlashLite`
   - `unsupportedCanaryFallsBackToDefaultModePolicy`
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 본 라운드는 `RouteRequest`/`ModelRouter` 중심 보정이므로, 각 Agent 요청 DTO의 `strictJsonRequired` 기본값(`true`) 정책은 별도 라운드에서 통합 정합성 점검이 필요함.
- strict-json 기본 정책은 `coordination/DECISIONS.md`의 D-018이 아직 Draft 상태이므로, Main 승인 이후 정책 문서 확정 반영이 필요함.

## 승인 필요 항목
- `application.yml` 변경: **없음**
- 공용 모델 변경: **없음**
- 빌드 설정 변경: **없음**
- 사전 승인 필요 항목 적용 여부: **해당 없음**
