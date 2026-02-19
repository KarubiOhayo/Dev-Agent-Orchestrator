# Code Agent API

## 개요

`/api/agents/code/generate`는 모델 라우팅 정책을 적용하고, 선택된 벤더 모델로 실제 LLM API를 호출합니다.
또한 응답 코드블록을 파싱해 `dry-run` 또는 실제 파일 쓰기(`apply=true`)까지 수행할 수 있습니다.
선택적으로 `chainToDoc=true`를 주면 Code 결과를 기반으로 DocAgent를 연쇄 실행합니다.
`chainToReview=true`를 주면 Code 결과를 기반으로 ReviewAgent를 연쇄 실행합니다.
`/api/agents/spec/generate`에서도 `chainToCode=true`와 `codeChainToDoc/codeChainToReview`를 함께 사용하면
Spec -> Code -> Doc/Review 원샷 체이닝을 실행할 수 있습니다.

`/api/agents/doc/generate`는 Code 산출물을 입력으로 받아 구조화된 문서(JSON 스키마)를 생성합니다.
`/api/agents/review/generate`는 Code 산출물을 입력으로 받아 구조화된 리뷰(JSON 스키마)를 생성합니다.

CLI 초안 사용법은 `/docs/cli-quickstart.md`를 참고하세요.

## 설정

`application.yml`의 아래 설정을 사용합니다.

- `devagent.model-routing`: Agent별 모델 선택/폴백 정책
- `devagent.llm`: 벤더별 base URL, API key, 생성 옵션
- `devagent.context`: rules/examples 선별 주입(top-k)
- `devagent.prompt`: 공통/에이전트/프로젝트 프롬프트 합성
- `devagent.run-state`: 실행 로그/프로젝트 메모리 저장(SQLite 우선, fallback 지원)

## API 키 주입

다음 환경 변수(또는 `.env`)를 읽습니다.

- `OPENAI_API_KEY`
- `ANTHROPIC_API_KEY`
- `GOOGLE_API_KEY` 또는 `GEMINI_API_KEY`

## 요청 예시

```bash
curl -X POST http://localhost:8080/api/agents/code/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인 API 스켈레톤을 만들어줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "largeContext": false,
    "strictJsonRequired": true,
    "apply": false,
    "overwriteExisting": false,
    "chainToDoc": true,
    "docUserRequest": "생성된 코드 기준으로 API/구조 문서를 작성해줘",
    "chainToReview": true,
    "reviewUserRequest": "보안/안정성 관점으로 우선순위 리뷰를 작성해줘",
    "chainFailurePolicy": "FAIL_FAST"
  }'
```

### Spec -> Code -> Doc/Review 원샷 체이닝 예시

```bash
curl -X POST http://localhost:8080/api/agents/spec/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인/토큰 재발급 명세를 JSON으로 작성해줘",
    "mode": "QUALITY",
    "riskLevel": "MEDIUM",
    "chainToCode": true,
    "codeUserRequest": "위 명세를 기준으로 코드를 생성해줘",
    "codeChainToDoc": true,
    "codeDocUserRequest": "생성된 코드 기준 API 문서를 작성해줘",
    "codeChainToReview": true,
    "codeReviewUserRequest": "보안/안정성 관점 리뷰를 작성해줘",
    "codeChainFailurePolicy": "PARTIAL_SUCCESS",
    "codeApply": false
  }'
```

## 응답 필드

- `runId`: 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `output`: 모델이 생성한 텍스트
- `files`: 파싱된 생성 파일 목록
  - 형식: `[{ "path": "relative/path", "content": "string" }]`
  - 의미: 모델 출력에서 추출한 "작성 대상 파일 초안"이며, `apply=false`여도 항상 반환될 수 있음
  - `applyResult`와의 관계: `files`는 "계획/파싱 결과", `applyResult`는 해당 파일들에 대해 실제 적용(또는 dry-run 평가)한 결과
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `applyResult`: 파싱 파일 수/쓰기 결과(`DRY_RUN`, `WRITTEN`, `SKIPPED`, `REJECTED`, `ERROR`)
- `chainedDocResult`: `chainToDoc=true`일 때 DocAgent 실행 결과
- `chainedReviewResult`: `chainToReview=true`일 때 ReviewAgent 실행 결과
- `chainFailures`: 체인 실패 구조화 목록
  - 형식: `[{ "agent": "DOC|REVIEW", "failedStage": "CHAIN_DOC|CHAIN_REVIEW", "errorMessage": "string" }]`
  - `chainFailurePolicy=PARTIAL_SUCCESS`일 때 체인 실패가 발생하면 해당 목록에 누적됨
  - `chainFailurePolicy=FAIL_FAST`일 때는 체인 실패 즉시 요청 실패로 전파되므로 일반적으로 빈 배열

## 체인 실패 정책

- `chainFailurePolicy` 기본값: `FAIL_FAST` (하위 호환)
- 선택값:
  - `FAIL_FAST`: Doc/Review 체인 실패 시 Code 요청 전체를 실패로 반환
  - `PARTIAL_SUCCESS`: 체인 실패를 `chainFailures`에 기록하고 Code 요청은 성공으로 반환
- `PARTIAL_SUCCESS`에서도 run-state 이벤트(`CHAIN_*_TRIGGERED/DONE/FAILED`) 기록 계약은 동일하게 유지됨
- `PARTIAL_SUCCESS`를 사용하는 클라이언트는 HTTP 200이어도 `chainFailures[]`를 반드시 확인해야 함
- CLI(`devagent generate/spec`)에서는 `--fail-on-chain-failures=true` 가드레일로 `chainFailures[]` 존재 시 종료코드 `3`을 강제할 수 있음(출력/응답 계약은 유지).

## 출력 파싱 fallback 관측 이벤트 (run-state)

- Code: `CODE_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: code 출력이 `files[]` JSON으로 직접 파싱되지 않고 markdown fallback 경로를 사용한 경우
  - 메시지 형식: `source=MARKDOWN_FALLBACK`
- Spec: `SPEC_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: spec 출력 파싱 source가 `DIRECT_JSON`이 아닌 경우
    - 경고 대상 source: `JSON_CODE_BLOCK`, `FALLBACK_SCHEMA`
  - 메시지 형식: `source=<PARSE_SOURCE>`
- Doc: `DOC_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: doc 출력 파싱이 fallback schema(`FALLBACK`) 경로를 사용한 경우
  - 메시지 형식: `source=FALLBACK`
- Review: `REVIEW_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: review 출력 파싱이 fallback schema(`FALLBACK`) 경로를 사용한 경우
  - 메시지 형식: `source=FALLBACK`

## fallback warning run-state 집계 기준 (운영 계약)

### 대상 이벤트

- `CODE_OUTPUT_FALLBACK_WARNING`
- `SPEC_OUTPUT_FALLBACK_WARNING`
- `DOC_OUTPUT_FALLBACK_WARNING`
- `REVIEW_OUTPUT_FALLBACK_WARNING`

### 집계 단위

- 기본 집계는 `agent별 일 단위(KST 00:00~23:59)`로 수행한다.
- 동일 기간의 `전체 집계(모든 agent 합산)`를 함께 계산한다.
- 보고서에는 이벤트별 건수, `parseEligibleRunCount`, `warningRate`, 임계치 판정 결과를 모두 포함한다.

### 모수/경고율 정의

- 경고율 산식:
  - `warningRate = warningEventCount / parseEligibleRunCount`
- `warningEventCount`:
  - 해당 집계 단위(일/agent 또는 전체)에서 발생한 `*_OUTPUT_FALLBACK_WARNING` 이벤트 수
- `parseEligibleRunCount`:
  - 동일 집계 단위에서 파싱 대상 출력을 생성한 **agent 서비스 run 수**
  - 해석 기준: API 직접 호출 run + 체인 호출로 내부 서비스가 실행된 run을 모두 포함
  - 체인 포함 원칙: 다른 agent 요청 흐름에서 내부적으로 해당 agent 서비스가 실행된 경우도 해당 agent의 모수에 포함
  - Code: Code 서비스 run 기준(직접 `POST /api/agents/code/generate` 호출 run + Spec `chainToCode=true`로 내부 Code 서비스가 실행된 run)
  - Spec: Spec 서비스 run 기준(직접 `POST /api/agents/spec/generate` 호출 run; 현재 체인 유입 경로 없음)
  - Doc: Doc 서비스 run 기준(직접 `POST /api/agents/doc/generate` 호출 run + `chainToDoc=true` 체인 run)
  - Review: Review 서비스 run 기준(직접 `POST /api/agents/review/generate` 호출 run + `chainToReview=true` 체인 run)
- 최소 샘플 수 조건:
  - `parseEligibleRunCount < 20`이면 판정 등급을 `INSUFFICIENT_SAMPLE`로 표시하고 임계치 판정/알림 트리거에서 제외한다.

### 임계치

| 등급 | 조건 (`warningRate`) | 운영 해석 |
|---|---|---|
| NORMAL | `< 0.05` | 정상 범위 |
| CAUTION | `>= 0.05` and `< 0.15` | 주의, 추세 관찰 필요 |
| WARNING | `>= 0.15` | 경고, 원인 분석 및 대응 필요 |

### 알림 룰

- 연속 초과:
  - 동일 agent가 `WARNING` 등급을 2일 연속 기록하면 알림을 발생시킨다.
- 급증:
  - 전일 대비 `warningRate`가 `+0.10`p 이상 상승하고 `warningEventCount`가 5건 이상 증가하면 알림을 발생시킨다.
- 전체 집계 보호 규칙:
  - 전체 집계 `warningRate >= 0.10`이면 agent별 상태와 별개로 일괄 점검 알림을 발생시킨다.
- `INSUFFICIENT_SAMPLE`은 알림 대상에서 제외하되, 보고서에 `표본 부족` 상태를 명시한다.

### 실측 보정 준비 체크 (H-015)

- 목적:
  - 임계치/알림 룰 수치 조정 전에 최근 14일 운영 데이터의 가용성과 해석 가능성을 먼저 검증한다.
- 관측 구간:
  - 최근 14일 `KST 00:00~23:59` 일 단위 집계를 기준으로 agent별/전체 집계를 함께 점검한다.
- 일별 상태 분류:
  - `집계 성공`: `parseEligibleRunCount`, `warningEventCount` 산출이 모두 성공한 경우
  - `집계 불가`: run-state 조회 실패, 기간 변환 실패, 필수 필드 누락/파싱 오류 등으로 산출이 불가능한 경우
  - `INSUFFICIENT_SAMPLE`: 집계 성공이지만 `parseEligibleRunCount < 20`인 경우(임계치 판정/알림 계산 제외)
- 보정 후보 수집 기준(수치 변경 없음):
  - `집계 성공` + 샘플 충분(`parseEligibleRunCount >= 20`) 구간에서 agent별 `warningRate` 분포(최소/중앙/상위 분위/최대)와 `warningEventCount` 추세를 기록한다.
  - 연속 `WARNING` 발생 빈도, 급증 규칙 충족 빈도, 전체 집계 보호 규칙 트리거 빈도를 기록한다.
  - `INSUFFICIENT_SAMPLE` 및 `집계 불가`는 임계치 후보 산정과 분리해 별도 통계(일수/비율/원인)로 관리한다.
- 보고 권장 항목:
  - 최근 14일 집계 성공/실패 일수
  - `INSUFFICIENT_SAMPLE` 일수 및 비율
  - `집계 불가` 원인 분류별 건수
  - 임계치 보정 판단 보류 여부와 근거

### 실측 보정 실행 결과 (H-016)

- 실행 라운드 기준 구간:
  - `2026-02-06 ~ 2026-02-19` (KST, 14일)
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)
- 대상 fallback warning 이벤트:
  - `CODE_OUTPUT_FALLBACK_WARNING`
  - `SPEC_OUTPUT_FALLBACK_WARNING`
  - `DOC_OUTPUT_FALLBACK_WARNING`
  - `REVIEW_OUTPUT_FALLBACK_WARNING`

#### 14일 가용성 게이트 판정 (전체)

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |

- 최종 판정:
  - **보정 보류**
- 보류 사유:
  - 최근 14일 모두 `parseEligibleRunCount < 20`으로 샘플 부족 상태
  - 샘플 충분(`parseEligibleRunCount >= 20`) 구간이 없어 후보값 신뢰도 검증 불가
- `집계 불가` 원인 분류:
  - 해당 없음 (`0일`)

#### 14일 누적 요약 (agent별/전체)

| 구분 | parseEligibleRunCount(14d) | warningEventCount(14d) | warningRate(14d) | 샘플 충분 일수(>=20) |
|---|---:|---:|---:|---:|
| CODE | 4 | 0 | 0.0000 | 0 |
| SPEC | 1 | 0 | 0.0000 | 0 |
| DOC | 0 | 0 | N/A | 0 |
| REVIEW | 0 | 0 | N/A | 0 |
| 전체 | 5 | 0 | 0.0000 | 0 |

#### 임계치/알림 룰 처리 결과

- 임계치 수치 유지:
  - `NORMAL < 0.05`
  - `CAUTION >= 0.05 and < 0.15`
  - `WARNING >= 0.15`
- 알림 룰 수치 유지:
  - 급증 기준 `+0.10p`
  - 전체 집계 보호 기준 `warningRate >= 0.10`
- 적용 전/후 비교:
  - **보정 보류에 따라 후보값 제시 및 전/후 비교를 수행하지 않음**

### H-017 샘플 확보 계획 (재보정 착수 준비)

- H-016 기준선(최근 14일, KST `2026-02-06 ~ 2026-02-19`):
  - 집계 성공 일수: `14일`
  - `INSUFFICIENT_SAMPLE` 일수/비율: `14일 / 1.00`
  - `집계 불가` 일수: `0일`
  - 누적 `parseEligibleRunCount`: `CODE 4`, `SPEC 1`, `DOC 0`, `REVIEW 0`, `전체 5`
  - 샘플 충분 일수(`parseEligibleRunCount >= 20`): `0일`

#### 재보정 착수 목표 (최근 14일 기준)

| 항목 | H-016 기준선 | H-017 목표 |
|---|---:|---:|
| 집계 성공 일수 | 14일 | >= 10일 |
| `INSUFFICIENT_SAMPLE` 비율 | 1.00 | <= 0.50 |
| `집계 불가` 일수 | 0일 | < 3일 |
| 샘플 충분 일수(`>=20`) | 0일 | >= 7일 |

#### 샘플 확보 실행안 (직접 호출 + 체인 호출)

- 시나리오 A (직접 Code 호출):
  - `POST /api/agents/code/generate`를 `apply=false`, 체인 비활성으로 일 `6회` 실행
  - 기대 모수 증가: `CODE +6/일`
- 시나리오 B (Spec -> Code 체인):
  - `POST /api/agents/spec/generate`를 `chainToCode=true`로 일 `4회` 실행
  - 기대 모수 증가: `SPEC +4/일`, `CODE +4/일`
- 시나리오 C (Code -> Doc/Review 체인):
  - `POST /api/agents/code/generate`를 `chainToDoc=true`, `chainToReview=true`로 일 `6회` 실행
  - 기대 모수 증가: `CODE +6/일`, `DOC +6/일`, `REVIEW +6/일`
- 일일 최소 모수 목표(전체 + agent):
  - `CODE >= 16`, `SPEC >= 4`, `DOC >= 6`, `REVIEW >= 6`, `전체 >= 32`
- 점검 주기:
  - 매일 야간 리포트(`A-001`, 09:00 KST)로 전일/최근 14일 추세를 점검
  - 당일 중간 점검 1회(수동)로 체인 실행 누락 여부를 보정

#### 목표 대비 진행률/예상치(Projection) 산출 기준

- 목표 대비 진행률:
  - `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)` (표기 범위 `0~100%`)
  - `목표 초과 일수 = max(0, 집계 성공 일수 - 10)` (목표 초과 달성 정보 분리 표기)
  - `샘플 부족 개선 진행률 = min(1, (1.00 - insuffRatio) / 0.50)` (H-016 기준선 1.00 대비)
  - `집계 불가 안정성 진행률 = min(1, (3 - 집계불가일수) / 3)`
- 게이트 충족 예상치(베스트 케이스):
  - `requiredSufficientDays = max(0, insufficientDays - 7)`
  - 최근 3일 평균 `parseEligibleRunCount(전체) >= 32`를 만족하면
    - `예상 재보정 착수 가능일 = 오늘 + requiredSufficientDays(일)`
- 미충족 원인 분류:
  - `LOW_TRAFFIC`: 전체 모수(`parseEligibleRunCount`)가 일일 목표 미달
  - `CHAIN_COVERAGE_GAP`: `SPEC`/`DOC`/`REVIEW` 중 1개 이상이 agent 목표 미달
  - `COLLECTION_FAILURE`: run-state 조회/파싱 실패로 `집계 불가` 발생

#### 재보정 착수/보류 분기 규칙

- `재보정 착수 가능`:
  - 최근 14일 기준 아래 4개 게이트를 모두 충족
    - `집계 성공 >= 10`
    - `INSUFFICIENT_SAMPLE 비율 <= 0.50`
    - `집계 불가 < 3`
    - `샘플 충분 일수(parseEligibleRunCount >= 20) >= 7`
  - 다음 액션: 임계치/알림 룰 후보값 산정 + 적용 전/후 영향 비교 라운드 착수
- `재보정 보류`:
  - 위 4개 게이트 중 1개라도 미충족
  - 다음 액션: 샘플 확보 실행안 유지/확대 + 미충족 원인 분류 기반 보완
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 재보정 착수 전까지 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙은 유지한다.

### H-018 운영 적용 점검 결과 (H-017 계획 적용)

- 점검 구간:
  - 최근 14일 KST `2026-02-06 ~ 2026-02-19`
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)
- 대상 fallback warning 이벤트:
  - `CODE_OUTPUT_FALLBACK_WARNING`
  - `SPEC_OUTPUT_FALLBACK_WARNING`
  - `DOC_OUTPUT_FALLBACK_WARNING`
  - `REVIEW_OUTPUT_FALLBACK_WARNING`

#### 최근 14일 실측 요약 (게이트 + agent 모수)

| 항목 | 실측값 | H-017 목표/기준 | 판정 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

| 구분 | parseEligibleRunCount(14d) | warningEventCount(14d) | warningRate(14d) |
|---|---:|---:|---:|
| CODE | 4 | 0 | 0.0000 |
| SPEC | 1 | 0 | 0.0000 |
| DOC | 0 | 0 | N/A |
| REVIEW | 0 | 0 | N/A |
| 전체 | 5 | 0 | 0.0000 |

- 일별 추세 요약:
  - 실측 모수 발생일: `2026-02-11`(전체 4), `2026-02-13`(전체 1)
  - 나머지 12일은 `parseEligibleRunCount=0`으로 샘플 부족 상태가 지속됨
  - 최근 3일 평균 전체 모수: `0.0000` (목표 `>= 32` 미충족)

#### H-017 목표 대비 진행률/미달률

| 항목 | 실측 | 목표 | 달성률 | 미달률/갭 |
|---|---:|---:|---:|---:|
| 집계 성공 일수 | 14일 | >= 10일 | 100% | 0일 |
| `INSUFFICIENT_SAMPLE` 비율 | 1.00 | <= 0.50 | 0% | +0.50 |
| `집계 불가` 일수 | 0일 | < 3일 | 100% | 0일 |
| 샘플 충분 일수(`>=20`) | 0일 | >= 7일 | 0% | -7일 |

- 목표 초과 달성 정보:
  - `집계 성공 목표 초과 일수 = 4일` (`14 - 10`)

#### Projection 대비 실측 오차 (H-018)

- H-017 Projection 기준:
  - `requiredSufficientDays = max(0, insufficientDays - 7)` -> `7일`
  - `최근 3일 평균 전체 모수 >= 32`일 때만 `예상 재보정 착수 가능일` 산정
- H-018 실측:
  - `insufficientDays=14`, `sufficientDays=0`, `insuffRatio=1.00`, 최근 3일 평균 전체 모수 `0.0000`

| 오차 항목 | H-017 예상(기준) | H-018 실측 | Delta | 허용 기준 | 초과 여부 |
|---|---|---|---:|---|---|
| `deltaSufficientDays` | 7일(필요 최소) | 0일 | -7일 | 절대오차 2일 | 초과 |
| `deltaInsufficientRatio` | 0.50(상한) | 1.00 | +0.50 | 절대오차 0.10 | 초과 |
| `deltaStartDate` | `2026-02-26`(조건부 최소 예상) | 미산정/보류 | N/A | 절대오차 2일 | 조건 미충족 |

- 종합 오차 판정:
  - `|deltaSufficientDays|=7 > 2`, `|deltaInsufficientRatio|=0.50 > 0.10`으로 허용 기준 초과
  - `deltaStartDate`는 전제조건(최근 3일 평균 전체 모수 `>= 32`) 미충족으로 산정 불가

#### 재보정 착수 가능/보류 판정

- 최종 판정:
  - **보정 보류**
- 근거:
  - 게이트 4개 중 2개(`INSUFFICIENT_SAMPLE` 비율, 샘플 충분 일수) 미충족
  - Projection 오차 허용 기준(일수/비율) 초과

#### 미충족 원인 분류 및 보완 액션 (우선순위)

1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: 최근 3일 평균 전체 모수 `0.0000` (목표 `>= 32` 대비 -32.0000)
   - 액션: Code 직접 호출/체인 호출 실행량을 일일 목표(`CODE 16`, `SPEC 4`, `DOC 6`, `REVIEW 6`)까지 증량
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: 14일 누적 `DOC 0`, `REVIEW 0`, `SPEC 1`로 체인 기반 모수 확보 실패
   - 액션: Spec->Code, Code->Doc/Review 체인 비중을 우선 상향하고 agent별 일일 목표 달성 여부를 일 단위로 점검
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: `집계 불가 0일`로 현재 미발생
   - 액션: run-state 조회/파싱 실패 감시 규칙 유지(발생 시 즉시 원인 분류 및 당일 복구)

#### 유지 원칙 재확인

- 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
- `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
- 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

### H-019 재보정 착수 가능 시점 재점검 (최신 14일)

- 실행일(KST):
  - `2026-02-19`
- 점검 구간:
  - 최근 14일 KST `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)
- 대상 fallback warning 이벤트:
  - `CODE_OUTPUT_FALLBACK_WARNING`
  - `SPEC_OUTPUT_FALLBACK_WARNING`
  - `DOC_OUTPUT_FALLBACK_WARNING`
  - `REVIEW_OUTPUT_FALLBACK_WARNING`

#### 4개 게이트 실측 + 판정

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

#### 진행률(상한 적용) + 목표 초과 일수

- `집계 성공 달성률 = min(1, 집계 성공 일수 / 10) = min(1, 14/10) = 100%`
- `목표 초과 일수 = max(0, 집계 성공 일수 - 10) = max(0, 14-10) = 4일`

#### Projection 재산정 (H-019)

- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14-7) = 7일`
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`) 실측:
  - `0.0000` (기준 `>= 32` 미충족)
- `예상 재보정 착수 가능일`:
  - **미산정** (최근 3일 평균 전체 모수 전제조건 미충족)
  - 참고: 전제조건 충족을 가정한 조건부 최소값은 `2026-02-26`(`2026-02-19 + 7일`)

#### 최근 14일 누적 요약 (agent별/전체)

| 구분 | parseEligibleRunCount(14d) | warningEventCount(14d) | warningRate(14d) |
|---|---:|---:|---:|
| CODE | 4 | 0 | 0.0000 |
| SPEC | 1 | 0 | 0.0000 |
| DOC | 0 | 0 | N/A |
| REVIEW | 0 | 0 | N/A |
| 전체 | 5 | 0 | 0.0000 |

- 일별 추세 요약:
  - 실측 모수 발생일: `2026-02-11`(전체 4), `2026-02-13`(전체 1)
  - 나머지 12일은 `parseEligibleRunCount=0`으로 샘플 부족 상태 유지
  - 최근 3일 평균 전체 모수: `0.0000`

#### READY/HOLD 최종 판정

- `recalibrationReadiness`: **HOLD**
- 미충족 게이트:
  - `INSUFFICIENT_SAMPLE 비율 <= 0.50` (실측 `1.00`)
  - `샘플 충분 일수 >= 7` (실측 `0일`)
- 판정 근거:
  - 게이트 4개 중 2개 미충족 상태가 유지되고 있으며, 최근 3일 모수 추세도 착수 전제조건(`>= 32`)을 충족하지 못함

#### HOLD 원인 분류 및 보완 액션 우선순위

1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: 최근 3일 평균 전체 모수 `0.0000` (목표 `>= 32` 미충족)
   - 액션: Code 직접 호출/체인 호출 실행량을 일일 목표(`CODE 16`, `SPEC 4`, `DOC 6`, `REVIEW 6`)까지 증량
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: 14일 누적 `DOC 0`, `REVIEW 0`, `SPEC 1`로 체인 기반 모수 확보 부족
   - 액션: Spec->Code, Code->Doc/Review 체인 비중을 상향하고 agent별 목표 달성 여부를 일 단위 점검
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: `집계 불가 0일`로 현재 미발생
   - 액션: run-state 조회/파싱 실패 감시 규칙 유지(발생 시 당일 원인 분류/복구)

### H-020 샘플 확보 실행률 추적 정합화 (최근 7일 + 최신 14일 게이트 연계)

- 실행일(KST):
  - `2026-02-19`
- 점검 구간:
  - 최신 14일 KST `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일 KST `2026-02-13 ~ 2026-02-19`
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)

#### 실행률 산식 (H-020 추가)

- agent별 달성률:
  - `achievementRate = min(1, actualRuns / targetRuns)`
- 전체 실행률:
  - `overallExecutionRate = min(1, totalActualRuns / 32)`
- 일일 목표(`targetRuns`) 고정값:
  - `CODE 16`, `SPEC 4`, `DOC 6`, `REVIEW 6` (합계 32)

#### 최신 14일 게이트 실측 + 판정 (유지)

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

#### 최근 7일 목표 대비 실제 실행량/달성률 (agent별 + 전체)

| 일자(KST) | CODE (`actual/16`) | SPEC (`actual/4`) | DOC (`actual/6`) | REVIEW (`actual/6`) | `totalActualRuns` | `overallExecutionRate` |
|---|---:|---:|---:|---:|---:|---:|
| `2026-02-13` | `1/16 (6.25%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 1 | 3.13% |
| `2026-02-14` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-15` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-16` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-17` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-18` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |
| `2026-02-19` | `0/16 (0.00%)` | `0/4 (0.00%)` | `0/6 (0.00%)` | `0/6 (0.00%)` | 0 | 0.00% |

#### 최근 7일 누적 실행률 요약 (`agentExecution`)

| 구분 | targetRuns(7d) | actualRuns(7d) | achievementRate(7d) |
|---|---:|---:|---:|
| CODE | 112 | 1 | 0.89% |
| SPEC | 28 | 0 | 0.00% |
| DOC | 42 | 0 | 0.00% |
| REVIEW | 42 | 0 | 0.00% |
| 전체 | 224 | 1 | 0.45% |

#### `LOW_TRAFFIC` 정량 근거 (최근 3일)

- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `0.0000` (기준 `>= 32` 미충족)
- 최근 3일 평균 `overallExecutionRate`: `0.0000`
- 해석:
  - 실행률 추세가 3일 연속 `0.00%`로 유지되어 샘플 충분 일수(`>=20`) 전환 신호가 없다.

#### `CHAIN_COVERAGE_GAP` 정량 근거 (실행률 기반)

- 최근 7일 `DOC`/`REVIEW` 실행량:
  - `DOC actualRuns=0`, `achievementRate=0.00%`
  - `REVIEW actualRuns=0`, `achievementRate=0.00%`
- 해석:
  - Code->Doc/Review 체인 유입이 관측되지 않아 agent 간 모수 균형이 붕괴된 상태가 지속된다.

#### Projection 재산정 (H-020)

- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14-7) = 7일`
- `예상 재보정 착수 가능일`:
  - **미산정** (최근 3일 평균 전체 모수 전제조건 `>= 32` 미충족)
  - 참고: 전제조건 충족을 가정한 조건부 최소값은 `2026-02-26`(`2026-02-19 + 7일`)

#### READY/HOLD 최종 판정

- `recalibrationReadiness`: **HOLD**
- `unmetGates`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- 판정 근거:
  - 최신 14일 게이트 4개 중 2개 미충족이 지속되고, 최근 7일 실행률 및 최근 3일 평균 실행률이 모두 저조해 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 해소 신호가 부재하다.

#### HOLD 원인 분류 및 보완 액션 우선순위 (실행률 반영)

1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: 최근 3일 평균 `parseEligibleRunCount=0.0000`, 최근 3일 평균 `overallExecutionRate=0.0000`
   - 액션: CODE/SPEC 직접 호출과 체인 호출을 일일 목표(`16/4`)까지 우선 증량
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: 최근 7일 `DOC/REVIEW` 실행률 `0.00%` 지속
   - 액션: `chainToDoc=true`, `chainToReview=true` 실행 비중을 늘리고 `DOC/REVIEW` 일일 목표(`6/6`) 추적을 고정
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: 최신 14일 `집계 불가 0일`
   - 액션: 현재 모니터링 규칙 유지(집계 실패 발생 시 당일 원인 분류/복구)

#### 유지 원칙 재확인

- 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
- `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
- 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

### H-021 실행량 증대 검증용 호출 믹스 추적 (최근 7일 직접/체인 분리)

- 실행일(KST):
  - `2026-02-19`
- 점검 구간:
  - 최신 14일 KST `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일 KST `2026-02-13 ~ 2026-02-19` (`today-6 ~ today`)
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)

#### 호출 믹스/실행률 산식 (H-021 추가)

- agent별 총 실행량:
  - `totalActualRuns = directRuns + chainRuns`
- agent별 체인 비중:
  - `chainShare = chainRuns / totalActualRuns`
  - 단, `totalActualRuns = 0`이면 `chainShare = 0`으로 처리한다.
- agent별 달성률:
  - `achievementRate = min(1, totalActualRuns / targetRuns)`
- 전체 실행률:
  - `overallExecutionRate = min(1, totalActualRunsAllAgents / 32)`
- 일일 목표(`targetRuns`) 고정값:
  - `CODE 16`, `SPEC 4`, `DOC 6`, `REVIEW 6` (합계 32)

#### 최신 14일 게이트 실측 + 판정 (유지)

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

#### 최근 7일 agent별 호출 믹스 누적 (`executionMix` + `agentExecution`)

| 구분 | targetRuns(7d) | directRuns(7d) | chainRuns(7d) | totalActualRuns(7d) | chainShare(7d) | achievementRate(7d) |
|---|---:|---:|---:|---:|---:|---:|
| CODE | 112 | 1 | 0 | 1 | 0.00% | 0.89% |
| SPEC | 28 | 0 | 0 | 0 | 0.00% | 0.00% |
| DOC | 42 | 0 | 0 | 0 | 0.00% | 0.00% |
| REVIEW | 42 | 0 | 0 | 0 | 0.00% | 0.00% |
| 전체 | 224 | 1 | 0 | 1 | 0.00% | 0.45% |

#### 최근 7일 일자별 전체 호출 믹스 + `overallExecutionRate`

| 일자(KST) | directRuns(All) | chainRuns(All) | totalActualRuns(All) | chainShare(All) | overallExecutionRate |
|---|---:|---:|---:|---:|---:|
| `2026-02-13` | 1 | 0 | 1 | 0.00% | 3.13% |
| `2026-02-14` | 0 | 0 | 0 | 0.00% | 0.00% |
| `2026-02-15` | 0 | 0 | 0 | 0.00% | 0.00% |
| `2026-02-16` | 0 | 0 | 0 | 0.00% | 0.00% |
| `2026-02-17` | 0 | 0 | 0 | 0.00% | 0.00% |
| `2026-02-18` | 0 | 0 | 0 | 0.00% | 0.00% |
| `2026-02-19` | 0 | 0 | 0 | 0.00% | 0.00% |

#### `LOW_TRAFFIC` 근거 (최근 3일)

- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `0.0000` (기준 `>= 32` 미충족)
- 최근 3일 평균 `overallExecutionRate`: `0.0000`
- 해석:
  - 최근 3일 실행률/모수가 모두 0으로 유지되어 샘플 충분 일수(`>=20`) 회복 신호가 없다.

#### `CHAIN_COVERAGE_GAP` 근거 (`DOC`/`REVIEW` 분리 고정)

| 구분 | directRuns(7d) | chainRuns(7d) | totalActualRuns(7d) | chainShare(7d) |
|---|---:|---:|---:|---:|
| DOC | 0 | 0 | 0 | 0.00% |
| REVIEW | 0 | 0 | 0 | 0.00% |

- 해석:
  - 최근 7일 `DOC`/`REVIEW` 체인 유입(`chainRuns`)이 0건으로 유지되어 체인 커버리지 결손이 지속된다.

#### Projection 재산정 (H-021)

- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14-7) = 7일`
- `예상 재보정 착수 가능일`:
  - **미산정** (최근 3일 평균 전체 모수 전제조건 `>= 32` 미충족)
  - 참고: 전제조건 충족을 가정한 조건부 최소값은 `2026-02-26`(`2026-02-19 + 7일`)

#### READY/HOLD 최종 판정

- `recalibrationReadiness`: **HOLD**
- `unmetGates`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- 판정 근거:
  - 최신 14일 게이트 4개 중 2개 미충족이 유지되고, 최근 7일 호출 믹스에서 `DOC`/`REVIEW` 체인 유입이 부재해 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP`가 동시에 지속된다.

#### HOLD 원인 분류 및 보완 액션 우선순위 (호출 믹스 반영)

1. `LOW_TRAFFIC` (우선순위 1)
   - 근거: 최근 3일 평균 `parseEligibleRunCount=0.0000`, 최근 3일 평균 `overallExecutionRate=0.0000`
   - 액션: CODE/SPEC 직접 호출 및 체인 호출 총량을 일일 목표(`16/4`)까지 우선 증량
2. `CHAIN_COVERAGE_GAP` (우선순위 2)
   - 근거: 최근 7일 `DOC`/`REVIEW` `chainRuns=0`, `chainShare=0.00%`
   - 액션: `chainToDoc=true`, `chainToReview=true` 호출 비중 상향 + `DOC/REVIEW` 목표(`6/6`) 일일 추적 고정
3. `COLLECTION_FAILURE` (우선순위 3)
   - 근거: 최신 14일 `집계 불가 0일`
   - 액션: 현재 모니터링 규칙 유지(집계 실패 발생 시 당일 원인 분류/복구)

#### 유지 원칙 재확인

- 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
- `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
- 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

### H-022 실행량 회복 액션 플랜 수립/운영 점검 (최근 7일 목표-실적 gap 고정)

- 실행일(KST):
  - `2026-02-19`
- 점검 구간:
  - 최신 14일 KST `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일 KST `2026-02-13 ~ 2026-02-19` (`today-6 ~ today`)
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)

#### 실행량 회복 산식 (H-022 추가)

- `executionRecoveryPlan`:
  - `targetDirectRuns`, `targetChainRuns`, `targetChainShare`
- `executionRecoveryProgress`:
  - `actualDirectRuns`, `actualChainRuns`, `executionGap`, `chainShareGap`
- 총량 산식:
  - `targetTotalRuns = targetDirectRuns + targetChainRuns`
  - `actualTotalRuns = actualDirectRuns + actualChainRuns`
  - `executionGap = targetTotalRuns - actualTotalRuns`
- 체인 비중 산식:
  - `actualChainShare = actualChainRuns / actualTotalRuns`
  - 단, `actualTotalRuns = 0`이면 `actualChainShare = 0`으로 처리한다.
  - `chainShareGap = targetChainShare - actualChainShare`
- 해석 규칙:
  - `LOW_TRAFFIC`는 `executionGap`을 중심으로 판정한다.
  - `CHAIN_COVERAGE_GAP`는 `chainShareGap`과 `DOC/REVIEW actualChainRuns`를 함께 판정한다.

#### 최신 14일 게이트 실측 + 판정 (유지)

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

#### 최근 7일 agent별 목표-실적 gap (`executionRecoveryPlan` + `executionRecoveryProgress`)

| 구분 | targetDirectRuns(7d) | targetChainRuns(7d) | targetTotalRuns(7d) | targetChainShare(7d) | actualDirectRuns(7d) | actualChainRuns(7d) | actualTotalRuns(7d) | actualChainShare(7d) | executionGap(7d) | chainShareGap(7d) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| CODE | 84 | 28 | 112 | 25.00% | 1 | 0 | 1 | 0.00% | 111 | 25.00%p |
| SPEC | 28 | 0 | 28 | 0.00% | 0 | 0 | 0 | 0.00% | 28 | 0.00%p |
| DOC | 0 | 42 | 42 | 100.00% | 0 | 0 | 0 | 0.00% | 42 | 100.00%p |
| REVIEW | 0 | 42 | 42 | 100.00% | 0 | 0 | 0 | 0.00% | 42 | 100.00%p |
| 전체 | 112 | 112 | 224 | 50.00% | 1 | 0 | 1 | 0.00% | 223 | 50.00%p |

#### `overallExecutionRate` 추세 + 원인 분류 근거

- 최근 7일 일자별 전체 `overallExecutionRate`:
  - `2026-02-13`: `3.13%`
  - `2026-02-14`: `0.00%`
  - `2026-02-15`: `0.00%`
  - `2026-02-16`: `0.00%`
  - `2026-02-17`: `0.00%`
  - `2026-02-18`: `0.00%`
  - `2026-02-19`: `0.00%`
- 최근 7일 누적 `overallExecutionRate`: `0.45%` (`1/224`)
- 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `0.0000` (기준 `>= 32` 미충족)
- 최근 3일 평균 `overallExecutionRate`: `0.0000`
- `DOC`/`REVIEW` 체인 커버리지(최근 7일):
  - `DOC actualChainRuns=0`, `chainShareGap=100.00%p`
  - `REVIEW actualChainRuns=0`, `chainShareGap=100.00%p`

#### Projection 재산정 (H-022)

- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14 - 7) = 7일`
- `예상 재보정 착수 가능일`:
  - **미산정** (최근 3일 평균 전체 모수 전제조건 `>= 32` 미충족)
  - 참고: 전제조건 충족 가정 시 조건부 최소값 `2026-02-26` (`2026-02-19 + 7일`)

#### READY/HOLD 최종 판정

- `recalibrationReadiness`: **HOLD**
- `unmetGates`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- 판정 근거:
  - 최신 14일 게이트 4개 중 2개 미충족 상태가 지속된다.
  - 최근 7일 전체 `executionGap=223`으로 실행 총량 부족(`LOW_TRAFFIC`)이 고정되었다.
  - 최근 7일 `DOC/REVIEW` 체인 비중 gap(`chainShareGap=100.00%p`)과 `actualChainRuns=0`이 지속되어 `CHAIN_COVERAGE_GAP`이 해소되지 않았다.

#### HOLD 시 일일 우선 액션 플랜 (목표-실적 gap 연계)

1. 직접 호출 증량 (`LOW_TRAFFIC`, 우선순위 1)
   - 일일 목표: `CODE targetDirectRuns/day=12`, `SPEC targetDirectRuns/day=4`
   - 실행: `POST /api/agents/code/generate` 직접 호출(`apply=false`) + `POST /api/agents/spec/generate` 호출 증량
   - 점검: 매일 `09:00 KST`(야간 리포트) + `17:00 KST`(중간 수동 점검), 담당 `운영 온콜`
2. 체인 호출 증량 (`CHAIN_COVERAGE_GAP`, 우선순위 2)
   - 일일 목표: `DOC targetChainRuns/day=6`, `REVIEW targetChainRuns/day=6`
   - 실행: `chainToDoc=true`, `chainToReview=true` 호출 비중 고정 및 누락 시 당일 보정
   - 점검: 매일 `09:00 KST` 리포트에서 `executionRecoveryProgress.actualChainRuns`/`chainShareGap` 확인, 담당 `운영 온콜`
3. 집계 안정성 유지 (`COLLECTION_FAILURE`, 우선순위 3)
   - 기준: 최신 14일 `집계 불가 0일` 유지
   - 실행: run-state 조회/파싱 실패 발생 시 당일 원인 분류와 재수집 수행

#### 유지 원칙 재확인

- 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
- `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
- 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

### H-023 실행량 회복 액션 이행률 추적/검증 (최근 7일 절대 gap + 직전 7일 delta)

- 실행일(KST):
  - `2026-02-19`
- 점검 구간:
  - 최신 14일 KST `2026-02-06 ~ 2026-02-19` (`today-13 ~ today`)
  - 최근 7일 KST `2026-02-13 ~ 2026-02-19` (`today-6 ~ today`)
  - 직전 7일 KST `2026-02-06 ~ 2026-02-12` (`today-13 ~ today-7`)
- 데이터 소스:
  - `storage/devagent.db` (`runs`, `run_events`)

#### H-023 추가 출력 계약 (`executionRecoveryTrend`, `recoveryActionStatus`)

- 기존 H-022 출력(`executionRecoveryPlan`, `executionRecoveryProgress`)은 유지한다.
- `executionRecoveryTrend`:
  - `executionGap`, `executionGapDelta`, `chainShareGap`, `chainShareGapDelta`
- `recoveryActionStatus`:
  - `cause`, `priority`, `status`, `evidence`

#### Delta 산식/해석 규칙 (H-023 추가)

- `executionGapDelta = executionGap(최근7일) - executionGap(직전7일)`
- `chainShareGapDelta = chainShareGap(최근7일) - chainShareGap(직전7일)`
- 개선 신호:
  - `executionGapDelta < 0` 또는 `chainShareGapDelta < 0`
- 미개선 신호:
  - `executionGapDelta >= 0` 그리고 `chainShareGapDelta >= 0`

#### 최신 14일 게이트 실측 + 판정 (유지)

| 항목 | 실측값 | 게이트 기준 | 결과 |
|---|---:|---:|---|
| 집계 성공 일수 | 14일 | >= 10일 | PASS |
| `INSUFFICIENT_SAMPLE` 일수/비율 | 14일 / 1.00 | <= 0.50 | FAIL |
| `집계 불가` 일수 | 0일 | < 3일 | PASS |
| 샘플 충분 일수(`parseEligibleRunCount >= 20`) | 0일 | >= 7일 | FAIL |

#### 최근 7일 agent별 절대 gap (`executionRecoveryTrend`)

| 구분 | targetDirectRuns(7d) | targetChainRuns(7d) | targetTotalRuns(7d) | targetChainShare(7d) | actualDirectRuns(7d) | actualChainRuns(7d) | actualTotalRuns(7d) | actualChainShare(7d) | executionGap(7d) | chainShareGap(7d) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| CODE | 84 | 28 | 112 | 25.00% | 1 | 0 | 1 | 0.00% | 111 | 25.00%p |
| SPEC | 28 | 0 | 28 | 0.00% | 0 | 0 | 0 | 0.00% | 28 | 0.00%p |
| DOC | 0 | 42 | 42 | 100.00% | 0 | 0 | 0 | 0.00% | 42 | 100.00%p |
| REVIEW | 0 | 42 | 42 | 100.00% | 0 | 0 | 0 | 0.00% | 42 | 100.00%p |
| 전체 | 112 | 112 | 224 | 50.00% | 1 | 0 | 1 | 0.00% | 223 | 50.00%p |

#### 최근 7일 vs 직전 7일 delta (`executionRecoveryTrend`)

| 구분 | executionGap(최근7일) | executionGap(직전7일) | executionGapDelta | chainShareGap(최근7일) | chainShareGap(직전7일) | chainShareGapDelta | 판정 |
|---|---:|---:|---:|---:|---:|---:|---|
| CODE | 111 | 109 | +2 | 25.00%p | 25.00%p | 0.00%p | 미개선 |
| SPEC | 28 | 27 | +1 | 0.00%p | 0.00%p | 0.00%p | 미개선 |
| DOC | 42 | 42 | 0 | 100.00%p | 100.00%p | 0.00%p | 미개선 |
| REVIEW | 42 | 42 | 0 | 100.00%p | 100.00%p | 0.00%p | 미개선 |
| 전체 | 223 | 220 | +3 | 50.00%p | 50.00%p | 0.00%p | 미개선 |

#### `overallExecutionRate` 추세 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거

- 최근 7일 일자별 전체 `overallExecutionRate`:
  - `2026-02-13`: `3.13%`
  - `2026-02-14`: `0.00%`
  - `2026-02-15`: `0.00%`
  - `2026-02-16`: `0.00%`
  - `2026-02-17`: `0.00%`
  - `2026-02-18`: `0.00%`
  - `2026-02-19`: `0.00%`
- 최근 7일 누적 `overallExecutionRate`: `0.45%` (`1/224`)
- 직전 7일 누적 `overallExecutionRate`: `1.79%` (`4/224`)
- `LOW_TRAFFIC` 근거:
  - 전체 `executionGapDelta=+3` (최근 7일 `223` vs 직전 7일 `220`)으로 악화
  - 최근 3일 평균 전체 모수(`parseEligibleRunCount`): `0.0000` (기준 `>= 32` 미충족)
  - 근거 runId(최근 7일): `ca487d6f-fa8c-4935-8781-ebe0048abb50`
- `CHAIN_COVERAGE_GAP` 근거:
  - `DOC`/`REVIEW` 최근 7일 `actualChainRuns=0`
  - `DOC`/`REVIEW` `chainShareGapDelta=0.00%p` (개선 없음)
  - 근거 집계표: 본 섹션 `executionRecoveryTrend` 표

#### Projection 재산정 (H-023)

- `requiredSufficientDays = max(0, insufficientDays - 7) = max(0, 14 - 7) = 7일`
- `예상 재보정 착수 가능일`:
  - **미산정** (최근 3일 평균 전체 모수 전제조건 `>= 32` 미충족)
  - 참고: 전제조건 충족 가정 시 조건부 최소값 `2026-02-26` (`2026-02-19 + 7일`)

#### READY/HOLD 최종 판정

- `recalibrationReadiness`: **HOLD**
- `unmetGates`:
  - `INSUFFICIENT_SAMPLE_RATIO` (`1.00 > 0.50`)
  - `SUFFICIENT_DAYS` (`0 < 7`)
- 판정 근거:
  - 최신 14일 게이트 4개 중 2개 미충족이 지속된다.
  - 최근 7일 vs 직전 7일 `executionGapDelta`/`chainShareGapDelta` 모두 개선 신호(<0)가 없다.

#### `recoveryActionStatus` (HOLD 시 원인별 액션 이행 상태)

| cause | priority | status | evidence |
|---|---:|---|---|
| `LOW_TRAFFIC` | 1 | `BLOCKED` | 최근 7일 `executionGap=223`, 직전 7일 대비 `executionGapDelta=+3`; 최근 3일 평균 `parseEligibleRunCount=0.0000`; runId `ca487d6f-fa8c-4935-8781-ebe0048abb50` |
| `CHAIN_COVERAGE_GAP` | 2 | `BLOCKED` | 최근 7일 `DOC/REVIEW actualChainRuns=0`, `chainShareGapDelta=0.00%p`; 체인 실행 근거 부재 |
| `COLLECTION_FAILURE` | 3 | `DONE` | 최신 14일 `집계 불가 0일`; run-state 집계 성공 14일 유지 |

#### 유지 원칙 재확인

- 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
- `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
- 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

## 공통 오류 응답 계약 (Routing + Agent API)

다음 엔드포인트는 입력 오류를 동일한 오류 envelope로 반환합니다.

- `POST /api/routing/resolve`
- `POST /api/agents/code/generate`
- `POST /api/agents/spec/generate`
- `POST /api/agents/doc/generate`
- `POST /api/agents/review/generate`

오류 응답 envelope:

```json
{
  "code": "MISSING_REQUIRED_FIELD",
  "message": "userRequest is required",
  "path": "/api/agents/spec/generate",
  "timestamp": "2026-02-19T02:40:56.123Z",
  "details": [
    {
      "field": "userRequest",
      "reason": "required",
      "rejectedValue": null
    }
  ]
}
```

- `code`: 오류 코드
- `message`: 사람이 읽을 수 있는 오류 메시지
- `path`: 요청 경로
- `timestamp`: UTC 시각(ISO-8601)
- `details[]`: 필드 단위 오류 상세(존재 시 포함)
- 복합 필수조건 누락(any-of)은 `code=MISSING_REQUIRED_ANY_OF`를 사용하고, 후보 필드 각각을 `details[]`에 `reason=any_of_required`로 기록

상태 코드/오류 코드 매핑:

- `400`: `MISSING_REQUIRED_FIELD`, `MISSING_REQUIRED_ANY_OF`, `REQUEST_BODY_REQUIRED`, `INVALID_ENUM_VALUE`, `MALFORMED_JSON`, `INVALID_JSON_VALUE`, `INVALID_JSON_REQUEST`, `INVALID_ARGUMENT`
- `500`: `INTERNAL_SERVER_ERROR`

오류 케이스 예시:

1) 필수 입력 누락/공백

```json
{
  "code": "MISSING_REQUIRED_FIELD",
  "message": "userRequest is required",
  "path": "/api/agents/spec/generate",
  "timestamp": "2026-02-19T02:40:56.123Z",
  "details": [
    {
      "field": "userRequest",
      "reason": "required",
      "rejectedValue": null
    }
  ]
}
```

2) 복합 필수조건(any-of) 누락

```json
{
  "code": "MISSING_REQUIRED_ANY_OF",
  "message": "userRequest or specInputPath is required",
  "path": "/api/agents/code/generate",
  "timestamp": "2026-02-19T03:17:11.300Z",
  "details": [
    {
      "field": "userRequest",
      "reason": "any_of_required",
      "rejectedValue": null
    },
    {
      "field": "specInputPath",
      "reason": "any_of_required",
      "rejectedValue": null
    }
  ]
}
```

3) enum 오류

```json
{
  "code": "INVALID_ENUM_VALUE",
  "message": "Invalid enum value",
  "path": "/api/agents/code/generate",
  "timestamp": "2026-02-19T02:41:00.101Z",
  "details": [
    {
      "field": "mode",
      "reason": "must be one of [COST_SAVER, BALANCED, QUALITY, GEMINI3_CANARY]",
      "rejectedValue": "NOT_A_MODE"
    }
  ]
}
```

4) JSON 파싱 오류

```json
{
  "code": "MALFORMED_JSON",
  "message": "Malformed JSON request body",
  "path": "/api/agents/review/generate",
  "timestamp": "2026-02-19T02:41:03.550Z",
  "details": []
}
```

5) 서버 오류

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "Internal server error",
  "path": "/api/agents/review/generate",
  "timestamp": "2026-02-19T02:41:10.777Z",
  "details": []
}
```

## Doc Agent 요청 예시

```bash
curl -X POST http://localhost:8080/api/agents/doc/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인 코드 기준 운영 문서를 생성해줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "largeContext": false,
    "strictJsonRequired": true,
    "codeRunId": "code-run-123",
    "codeOutput": "{\"files\":[...]}",
    "codeFiles": [
      {"path":"src/main/java/me/example/AuthController.java","content":"..."}
    ]
  }'
```

## Doc Agent 응답 필드

- `runId`: Doc 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `document`: 문서 JSON 스키마 결과
  - 형식:
    - `title`: 문자열
    - `summary`: 문자열
    - `sections`: `[{ "heading": "string", "content": "string" }]`
    - `relatedFiles`: `["relative/path"]`
    - `notes`: `["string"]`
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `sourceCodeRunId`: 체인 입력으로 사용된 Code run ID(없으면 `null`)

## Review Agent 요청 예시

```bash
curl -X POST http://localhost:8080/api/agents/review/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "생성 코드의 잠재 버그와 개선 포인트를 점검해줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "largeContext": false,
    "strictJsonRequired": true,
    "codeRunId": "code-run-123",
    "codeOutput": "{\"files\":[...]}",
    "codeFiles": [
      {"path":"src/main/java/me/example/AuthController.java","content":"..."}
    ]
  }'
```

## Review Agent 응답 필드

- `runId`: Review 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `review`: 리뷰 JSON 스키마 결과
  - 형식:
    - `summary`: 문자열
    - `overallRisk`: `LOW|MEDIUM|HIGH|CRITICAL`
    - `findings`: `[{ "title": "string", "severity": "LOW|MEDIUM|HIGH|CRITICAL", "file": "relative/path", "line": 0, "description": "string", "suggestion": "string" }]`
    - `strengths`: `["string"]`
    - `nextActions`: `["string"]`
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `sourceCodeRunId`: 체인 입력으로 사용된 Code run ID(없으면 `null`)
