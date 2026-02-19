# H-025 Spec -> Code 체인에서 Code의 Doc/Review 체인 옵션 전파 + CLI 옵션/출력 보강
Owner: WT-25 (`codex/h025-spec-code-chain-propagate-doc-review-cli`)
Priority: High

## 목표
- Spec 요청 1회로 Spec -> Code -> (Doc/Review)까지 원샷 체이닝이 가능하도록 한다.
- CLI에서 generate/spec 모두 Doc/Review 체인 옵션을 노출해 실제 사용/트래픽을 늘린다.
- CLI 출력에서 체인 실행 여부 및 chainFailures를 확인 가능하게 해 “PARTIAL_SUCCESS 누락 리스크”를 줄인다.

## 작업 범위
### A) API/체인 동작
- SpecGenerateRequest에 아래 필드를 추가한다(하위호환, 기본 false):
  - codeChainToDoc (boolean)
  - codeDocUserRequest (String)
  - codeChainToReview (boolean)
  - codeReviewUserRequest (String)
  - codeChainFailurePolicy (CodeGenerateRequest.ChainFailurePolicy)
- SpecCodeChainService에서 Spec -> Code 체인 시 위 옵션을 CodeGenerateRequest로 전파한다.

### B) CLI 기능
- devagent generate:
  - --chain-to-doc, --doc-user-request
  - --chain-to-review, --review-user-request
  - --chain-failure-policy (FAIL_FAST|PARTIAL_SUCCESS)
- devagent spec:
  - --chain-to-code는 유지
  - Code 체인 옵션은 prefix로 노출:
    - --code-chain-to-doc, --code-doc-user-request
    - --code-chain-to-review, --code-review-user-request
    - --code-chain-failure-policy

### C) CLI 출력(UX)
- human 출력(generate/spec) summary에 아래 항목 추가:
  - chainedDoc, chainedReview, chainFailures(count)
- json 출력(generate/spec)에도 summary에 위 항목 추가 + chainFailures 배열(구조화) 추가
  - 기존 필드(summary/fileResults)는 유지(호환)

### D) 문서 업데이트
- docs/cli-quickstart.md: 신규 옵션/예시 반영
- docs/code-agent-api.md: spec endpoint 사용 예시에 “Spec -> Code -> Doc/Review 원샷” 예시 추가

## 수용 기준
1) 기존 Spec 요청(체인 옵션 미사용)은 동작이 변경되지 않는다.
2) Spec API에서 chainToCode=true + codeChainToDoc=true + codeChainToReview=true가 설정되면:
   - Code run에서 doc/review 체인이 실행되며, 응답(chainedCodeResult) 내부에 chainedDocResult/chainedReviewResult가 채워진다.
3) CLI:
   - devagent generate/spec에서 신규 옵션이 "unknown option" 없이 처리된다.
   - 출력 summary에서 chainedDoc/chainedReview/chainFailures를 확인할 수 있다.
4) 테스트:
   - ./gradlew clean test --no-daemon 통과

## 비범위
- fallback-warning(H-0xx) 임계치/알림룰/거버넌스(H-024) 추가 변경
- run-state 스키마 변경
- 자동 커밋/PR 생성/웹훅(Plan A 위반)

## 제약
- handoff 범위 밖 수정 금지
- 공통 파일(application.yml, 빌드 설정) 변경 필요 시 중단 후 Main 승인 요청만 남길 것

## 보고서
- 완료 시 coordination/REPORTS/H-025-result.md 생성
- coordination/RELAYS/H-025-executor-to-review.md 생성(템플릿 기반)
- 보고서 필수 포함:
  - 변경 파일 목록
  - Spec API curl 예시(원샷 체이닝) + 결과 요약
  - CLI 예시(원샷 체이닝) + 출력 샘플
  - 테스트 명령 및 결과
  - 남은 리스크(호환성/출력 계약)
