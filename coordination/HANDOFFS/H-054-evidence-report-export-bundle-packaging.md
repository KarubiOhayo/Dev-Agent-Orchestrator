# H-054 evidence / report export bundle packaging

Owner: WT-54 (`codex/h054-evidence-report-export-bundle-packaging`)
Priority: Highest

## 목표
- H-053에서 만든 guided demo path 뒤에 바로 건넬 수 있는 external-facing evidence / report export bundle 문서를 만든다.
- README -> case study -> walkthrough가 설명한 내용을 어떤 source 문서/결과 보고/리뷰 산출물이 뒷받침하는지 한 번에 보여 주는 proof package를 정리한다.
- active roadmap와 parked fallback-warning 정책을 유지하면서 portfolio package의 "보여 주기 이후 증빙" 공백을 줄인다.

## 작업 범위
- 신규/수정 허용:
  - `docs/evidence-report-export-bundle.md` (신규)
  - `README.md` (필요 시 docs map 또는 read-next 링크 추가 수준의 최소 수정만 허용)
  - `docs/demo-showcase-walkthrough.md` (필요 시 evidence bundle 링크 추가 수준의 최소 수정만 허용)
  - `docs/portfolio-case-study.md` (필요 시 current limits 또는 read-together 링크 추가 수준의 최소 수정만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `README.md`
  - `docs/demo-showcase-walkthrough.md`
  - `docs/portfolio-case-study.md`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `docs/codex-ops-workflow.md`
  - `coordination/REPORTS/H-050-result.md`
  - `coordination/REPORTS/H-051-result.md`
  - `coordination/REPORTS/H-051-review.md`
  - `coordination/REPORTS/H-052-result.md`
  - `coordination/REPORTS/H-052-review.md`
  - `coordination/REPORTS/H-053-result.md`
  - `coordination/REPORTS/H-053-review.md`
  - `coordination/RELAYS/H-053-review-to-main.md`

## 구현 지침
- evidence / report export bundle은 coordination 이력 전체를 복사해 늘어놓는 것이 아니라, 외부 평가자/협업자에게 "무엇을 보면 이 프로젝트의 실제 증거가 되는가"를 설명하는 curated package여야 한다.
- 문서는 아래 질문에 답할 수 있게 구성한다.
  1. 이 bundle은 누구를 위한 것이며, demo 후 어떤 상황에서 건네는가?
  2. 어떤 문서/결과 보고/리뷰 근거를 포함해야 하고, 각 항목은 무엇을 증명하는가?
  3. 어떤 항목은 바로 외부 공유 가능하고, 어떤 항목은 내부 참고 또는 선택 첨부로 다뤄야 하는가?
  4. 공유 전에 어떤 redaction / safety / honesty guardrail을 확인해야 하는가?
  5. walkthrough나 README/case study가 바뀌면 bundle을 어떻게 유지보수해야 하는가?
- 권장 섹션:
  1. Bundle goal / audience / when to send
  2. Bundle contents overview (`narrative docs`, `demo companion`, `proof artifacts`, `ops/governance evidence`)
  3. Source-of-truth mapping table (`bundle item | source file | what it proves | shareability note`)
  4. Suggested export order 또는 folder layout
  5. Guardrails / redaction / what not to include
  6. How to use after the walkthrough
  7. Maintenance checklist / read next
- 증거 source는 현재 저장소에 이미 있는 문서/결과 보고/리뷰/릴레이만 사용한다. 없는 운영 성과, 외부 사용자 수, 배포 상태, fabricated output, 스크린샷, 꾸며낸 metrics를 추가하지 않는다.
- recent portfolio packaging rounds(`H-050`~`H-053`)와 stable docs를 evidence backbone으로 우선 사용한다.
- parked fallback-warning 트랙은 default bundle content가 아니다. governance 사례로 아주 짧게 언급할 수는 있지만, current proof package의 대표 근거처럼 전면화하지 않는다.
- `README.md`, walkthrough, case study를 수정하더라도 evidence bundle 발견성과 read-next 연결을 위한 최소 링크 수준에 머문다. 본문 전체를 다시 쓰지 않는다.
- 이번 라운드는 docs-only 패키징에 한정한다. source 문서를 복제한 export 폴더/zip 생성이나 새 live run 캡처는 비범위다.

## 수용 기준
1. `docs/evidence-report-export-bundle.md`가 생성되고, 외부 공유 가능한 evidence / report package의 guided path를 제공한다.
2. bundle이 `narrative docs`, `demo companion`, `proof artifacts`, `ops/governance evidence`를 구분해 설명한다.
3. 각 bundle 항목이 현재 존재하는 source 문서/결과 보고/리뷰/릴레이에 매핑되고, 무엇을 증명하는지와 공유 가능 범위를 함께 적는다.
4. redaction / honesty guardrail이 드러나고, fabricated output이나 parked fallback-warning 전면화가 없다.
5. `README.md`, `docs/demo-showcase-walkthrough.md`, `docs/portfolio-case-study.md`가 수정될 경우 evidence bundle 발견성을 위한 최소 링크 수준에 머문다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- source 문서를 복제한 별도 export 폴더/zip 생성
- walkthrough / case study / README 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개
- 실제 live demo 재실행, 스크린샷 제작, metrics 생성

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- bundle은 "증거를 어떤 순서와 기준으로 보여 줄 것인가"를 문서로 정리하는 작업이지, 새로운 실적을 만들어 내는 작업이 아니다
- 외부 공유 가능한 항목과 내부 참고용 항목을 혼동하지 않도록 shareability note를 분명히 적는다

## 보고서
- 완료 시 `coordination/REPORTS/H-054-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-054-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - bundle 섹션별 구성 요약
  - bundle item / source 문서 매핑 요약
  - 링크 변경 여부와 이유
  - redaction / sharing guardrail 선택 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안(`polishing`, `external-facing proof package` 관점)
