# [H-007] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-007-routing-strict-json-policy.md`
- result: `coordination/REPORTS/H-007-result.md`

## 구현 요약
- `RouteRequest.strictJsonRequired`를 tri-state(`Boolean`)로 변경해 미지정/null/명시값 구분 가능하게 조정.
- strict-json escalation은 `strictJsonRequired=true` 명시 요청에서만 적용되도록 변경.
- `ModelRouter` escalation 우선순위를 `review-high-risk -> strict-json -> large-context`로 고정.
- 동시 조건에서 primary/fallback/reasons 순서를 검증하는 회귀 테스트 추가.

## 변경 파일
- `src/main/java/me/karubidev/devagent/orchestration/routing/RouteRequest.java`
- `src/main/java/me/karubidev/devagent/orchestration/routing/ModelRouter.java`
- `src/test/java/me/karubidev/devagent/orchestration/routing/ModelRouterTest.java`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 추가 회귀 테스트
- `routeCodeBalancedWithoutStrictJsonEscalationUsesCodexByDefault`
- `routeCodeBalancedNullStrictJsonDoesNotEscalate`
- `routeCodeBalancedWithStrictJsonEscalatesToStrictJsonModel`
- `reviewHighRiskStrictJsonAndLargeContextPreservesEscalationPriorityOrder`

## 리뷰 집중 포인트
1. `RouteRequest` tri-state 해석이 Jackson 바인딩 및 기존 호출 경로에서 안전한지
2. escalation 적용/이유 기록 순서가 정책(`review -> strict-json -> large-context`)과 정확히 일치하는지
3. 후보 중복 제거(`LinkedHashSet`) 이후 fallback 순서가 의도대로 유지되는지
4. 기존 회귀(`COST_SAVER`, canary fallback) 검증 범위가 충분한지

## 알려진 리스크 / 오픈 이슈
- Agent 요청 DTO(`CodeGenerateRequest`, `SpecGenerateRequest`, `DocGenerateRequest`, `ReviewGenerateRequest`)의 `strictJsonRequired` 기본값 정합성은 본 handoff 비범위로 남아 있음.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-007-review.md` 작성
- P1/P2/P3 심각도 기준, 파일/라인 근거 포함
- 최종 결론을 `No findings` 또는 `LOW/MEDIUM/HIGH`로 명시
