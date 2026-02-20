#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-local}"
export GRADLE_USER_HOME

if [[ ! -x "./devagent" ]]; then
  echo "[seed] ./devagent 실행 파일을 찾을 수 없습니다." >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "[seed] python3가 필요합니다." >&2
  exit 1
fi

SEED_DIRECT_RUNS="${SEED_DIRECT_RUNS:-1}"
SEED_CHAIN_RUNS="${SEED_CHAIN_RUNS:-1}"
SEED_APPLY="${SEED_APPLY:-false}"
SEED_MODE="${SEED_MODE:-BALANCED}"
SEED_FAIL_FAST="${SEED_FAIL_FAST:-true}"
SEED_DB_PATH="${SEED_DB_PATH:-storage/devagent.db}"

SEED_SPEC_OUTPUT_DIR="${SEED_SPEC_OUTPUT_DIR:-storage/devagent-specs}"
SEED_LOG_DIR="${SEED_LOG_DIR:-storage/fallback-warning-seed}"
SEED_TIMESTAMP="${SEED_TIMESTAMP:-$(date +%Y%m%d-%H%M%S)}"

SEED_DIRECT_REQUEST_PREFIX="${SEED_DIRECT_REQUEST_PREFIX:-fallback-warning direct seed}"
SEED_CHAIN_SPEC_REQUEST_PREFIX="${SEED_CHAIN_SPEC_REQUEST_PREFIX:-fallback-warning chain seed spec}"
SEED_CHAIN_CODE_REQUEST_PREFIX="${SEED_CHAIN_CODE_REQUEST_PREFIX:-fallback-warning chain seed code}"
SEED_CHAIN_DOC_REQUEST_PREFIX="${SEED_CHAIN_DOC_REQUEST_PREFIX:-fallback-warning chain seed doc}"
SEED_CHAIN_REVIEW_REQUEST_PREFIX="${SEED_CHAIN_REVIEW_REQUEST_PREFIX:-fallback-warning chain seed review}"

RUN_LOG="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}.log"
RUN_RECORDS="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}-records.jsonl"
SNAPSHOT_BEFORE="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}-before.json"
SNAPSHOT_AFTER="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}-after.json"
SUMMARY_JSON="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}-summary.json"

mkdir -p "$SEED_SPEC_OUTPUT_DIR" "$SEED_LOG_DIR"
mkdir -p "$GRADLE_USER_HOME"
: > "$RUN_LOG"
: > "$RUN_RECORDS"

to_lower() {
  echo "$1" | tr "[:upper:]" "[:lower:]"
}

as_bool() {
  local raw
  raw="$(to_lower "${1:-}")"
  [[ "$raw" == "1" || "$raw" == "true" || "$raw" == "yes" || "$raw" == "y" ]]
}

require_non_negative_int() {
  local name="$1"
  local value="$2"
  if [[ ! "$value" =~ ^[0-9]+$ ]]; then
    echo "[seed] $name 값이 정수가 아닙니다: $value" >&2
    exit 1
  fi
}

require_non_negative_int "SEED_DIRECT_RUNS" "$SEED_DIRECT_RUNS"
require_non_negative_int "SEED_CHAIN_RUNS" "$SEED_CHAIN_RUNS"

if [[ ! -f "$SEED_DB_PATH" ]]; then
  echo "[seed] run-state DB 파일을 찾을 수 없습니다: $SEED_DB_PATH" >&2
  exit 1
fi

log() {
  local message="$1"
  local ts
  ts="$(date "+%Y-%m-%d %H:%M:%S")"
  printf "[%s] %s\n" "$ts" "$message" | tee -a "$RUN_LOG"
}

json_value() {
  local file_path="$1"
  local dotted_path="$2"
  python3 - "$file_path" "$dotted_path" <<'PY'
import json
import sys

file_path = sys.argv[1]
dotted_path = sys.argv[2]

try:
    with open(file_path, "r", encoding="utf-8") as handle:
        raw = handle.read().strip()
    obj = json.loads(raw) if raw else {}
except Exception:
    print("")
    raise SystemExit(0)

value = obj
for key in dotted_path.split("."):
    if not key:
        continue
    if isinstance(value, dict):
        value = value.get(key)
    else:
        value = None
    if value is None:
        break

if value is None:
    print("")
elif isinstance(value, (dict, list)):
    print(json.dumps(value, ensure_ascii=False))
else:
    print(value)
PY
}

snapshot_run_state() {
  local output_path="$1"
  python3 - "$SEED_DB_PATH" > "$output_path" <<'PY'
import datetime as dt
import json
import sqlite3
import sys

db_path = sys.argv[1]
conn = sqlite3.connect(db_path)
conn.row_factory = sqlite3.Row

now_utc = dt.datetime.now(dt.timezone.utc)
window_7d = now_utc - dt.timedelta(days=7)
window_14d = now_utc - dt.timedelta(days=14)
window_48h = now_utc - dt.timedelta(hours=48)

def scalar(query, params):
    row = conn.execute(query, params).fetchone()
    return int(row[0]) if row and row[0] is not None else 0

def agent_counts(window_start):
    rows = conn.execute(
        """
        SELECT agent, COUNT(*) AS cnt
        FROM runs
        WHERE created_at >= ?
        GROUP BY agent
        """,
        (window_start.isoformat().replace("+00:00", "Z"),),
    ).fetchall()
    out = {"CODE": 0, "SPEC": 0, "DOC": 0, "REVIEW": 0}
    for row in rows:
        out[row["agent"]] = int(row["cnt"])
    return out

counts_7d = agent_counts(window_7d)
counts_14d = agent_counts(window_14d)

chain_code_7d = scalar(
    "SELECT COUNT(*) FROM run_events WHERE event_type = 'CHAIN_CODE_DONE' AND created_at >= ?",
    (window_7d.isoformat().replace("+00:00", "Z"),),
)
chain_doc_7d = scalar(
    "SELECT COUNT(*) FROM run_events WHERE event_type = 'CHAIN_DOC_DONE' AND created_at >= ?",
    (window_7d.isoformat().replace("+00:00", "Z"),),
)
chain_review_7d = scalar(
    "SELECT COUNT(*) FROM run_events WHERE event_type = 'CHAIN_REVIEW_DONE' AND created_at >= ?",
    (window_7d.isoformat().replace("+00:00", "Z"),),
)

fresh_code_48h = scalar(
    "SELECT COUNT(*) FROM runs WHERE agent = 'CODE' AND created_at >= ?",
    (window_48h.isoformat().replace("+00:00", "Z"),),
)
fresh_chain_doc_48h = scalar(
    "SELECT COUNT(*) FROM run_events WHERE event_type = 'CHAIN_DOC_DONE' AND created_at >= ?",
    (window_48h.isoformat().replace("+00:00", "Z"),),
)
fresh_chain_review_48h = scalar(
    "SELECT COUNT(*) FROM run_events WHERE event_type = 'CHAIN_REVIEW_DONE' AND created_at >= ?",
    (window_48h.isoformat().replace("+00:00", "Z"),),
)

latest_row = conn.execute("SELECT MAX(created_at) AS latest FROM runs").fetchone()

payload = {
    "generatedAtUtc": now_utc.replace(microsecond=0).isoformat().replace("+00:00", "Z"),
    "window7d": {
        "runCounts": counts_7d,
        "chainDoneCounts": {
            "CODE_FROM_SPEC": chain_code_7d,
            "DOC_FROM_CODE": chain_doc_7d,
            "REVIEW_FROM_CODE": chain_review_7d,
        },
        "estimatedDirectCounts": {
            "CODE": max(0, counts_7d["CODE"] - chain_code_7d),
            "SPEC": counts_7d["SPEC"],
            "DOC": max(0, counts_7d["DOC"] - chain_doc_7d),
            "REVIEW": max(0, counts_7d["REVIEW"] - chain_review_7d),
        },
    },
    "window14d": {
        "runCounts": counts_14d,
    },
    "freshness48h": {
        "freshCodeRuns": fresh_code_48h,
        "freshChainDocDoneEvents": fresh_chain_doc_48h,
        "freshChainReviewDoneEvents": fresh_chain_review_48h,
    },
    "latestRunCreatedAtUtc": latest_row["latest"] if latest_row else None,
}

print(json.dumps(payload, ensure_ascii=False, indent=2))
PY
}

extract_chain_mapping() {
  local spec_run_id="$1"
  python3 - "$SEED_DB_PATH" "$spec_run_id" <<'PY'
import json
import re
import sqlite3
import sys

db_path = sys.argv[1]
spec_run_id = sys.argv[2]
conn = sqlite3.connect(db_path)
conn.row_factory = sqlite3.Row

def latest_payload(run_id, event_type):
    row = conn.execute(
        """
        SELECT payload
        FROM run_events
        WHERE run_id = ? AND event_type = ?
        ORDER BY created_at DESC
        LIMIT 1
        """,
        (run_id, event_type),
    ).fetchone()
    return row["payload"] if row else ""

def extract_id(payload, key):
    match = re.search(rf"{re.escape(key)}=([0-9a-fA-F-]+)", payload or "")
    return match.group(1) if match else ""

code_run_id = extract_id(latest_payload(spec_run_id, "CHAIN_CODE_DONE"), "codeRunId")
doc_run_id = extract_id(latest_payload(code_run_id, "CHAIN_DOC_DONE"), "docRunId") if code_run_id else ""
review_run_id = extract_id(latest_payload(code_run_id, "CHAIN_REVIEW_DONE"), "reviewRunId") if code_run_id else ""

events = []
for run_id in [spec_run_id, code_run_id]:
    if not run_id:
        continue
    rows = conn.execute(
        """
        SELECT event_type, payload, created_at
        FROM run_events
        WHERE run_id = ? AND event_type LIKE 'CHAIN_%'
        ORDER BY created_at
        """,
        (run_id,),
    ).fetchall()
    for row in rows:
        events.append(
            {
                "runId": run_id,
                "eventType": row["event_type"],
                "payload": row["payload"],
                "createdAt": row["created_at"],
            }
        )

print(
    json.dumps(
        {
            "specRunId": spec_run_id,
            "codeRunId": code_run_id,
            "docRunId": doc_run_id,
            "reviewRunId": review_run_id,
            "events": events,
        },
        ensure_ascii=False,
    )
)
PY
}

append_record() {
  local kind="$1"
  local index="$2"
  local exit_code="$3"
  local run_id="$4"
  local chain_failures="$5"
  local spec_output_path="$6"
  local code_run_id="$7"
  local doc_run_id="$8"
  local review_run_id="$9"
  local stdout_file="${10}"
  local stderr_file="${11}"
  local command_text="${12}"
  local chain_events_json="${13}"
  python3 - "$RUN_RECORDS" "$kind" "$index" "$exit_code" "$run_id" "$chain_failures" "$spec_output_path" "$code_run_id" "$doc_run_id" "$review_run_id" "$stdout_file" "$stderr_file" "$command_text" "$chain_events_json" <<'PY'
import json
import sys

(
    records_path,
    kind,
    index,
    exit_code,
    run_id,
    chain_failures,
    spec_output_path,
    code_run_id,
    doc_run_id,
    review_run_id,
    stdout_file,
    stderr_file,
    command_text,
    chain_events_json,
) = sys.argv[1:]

event_payload = []
if chain_events_json:
    try:
        parsed = json.loads(chain_events_json)
        event_payload = parsed.get("events", [])
    except Exception:
        event_payload = []

record = {
    "kind": kind,
    "index": int(index),
    "exitCode": int(exit_code),
    "runId": run_id,
    "chainMode": kind == "CHAIN",
    "chainFailures": int(chain_failures) if str(chain_failures).strip() else 0,
    "specOutputPath": spec_output_path or None,
    "codeRunId": code_run_id or None,
    "docRunId": doc_run_id or None,
    "reviewRunId": review_run_id or None,
    "stdoutFile": stdout_file,
    "stderrFile": stderr_file,
    "command": command_text.strip(),
    "chainEvents": event_payload,
}

with open(records_path, "a", encoding="utf-8") as handle:
    handle.write(json.dumps(record, ensure_ascii=False) + "\n")
PY
}

build_summary() {
  python3 - "$RUN_RECORDS" "$SNAPSHOT_BEFORE" "$SNAPSHOT_AFTER" > "$SUMMARY_JSON" <<'PY'
import json
import sys

records_path, before_path, after_path = sys.argv[1:]

records = []
with open(records_path, "r", encoding="utf-8") as handle:
    for line in handle:
        line = line.strip()
        if not line:
            continue
        records.append(json.loads(line))

with open(before_path, "r", encoding="utf-8") as handle:
    before = json.load(handle)

with open(after_path, "r", encoding="utf-8") as handle:
    after = json.load(handle)

success_count = sum(1 for item in records if item.get("exitCode") == 0 and item.get("runId"))
failure_count = sum(1 for item in records if item.get("exitCode") != 0 or not item.get("runId"))
chain_count = sum(1 for item in records if item.get("chainMode"))
direct_count = len(records) - chain_count

payload = {
    "records": records,
    "summary": {
        "totalRuns": len(records),
        "successRuns": success_count,
        "failedRuns": failure_count,
        "directRuns": direct_count,
        "chainRuns": chain_count,
    },
    "before": before,
    "after": after,
}

print(json.dumps(payload, ensure_ascii=False, indent=2))
PY
}

run_count=0
failure_count=0

snapshot_run_state "$SNAPSHOT_BEFORE"
log "fallback-warning seed 시작: direct=$SEED_DIRECT_RUNS, chain=$SEED_CHAIN_RUNS, mode=$SEED_MODE, apply=$SEED_APPLY, failFast=$SEED_FAIL_FAST"
log "run-state snapshot(before): $SNAPSHOT_BEFORE"

run_single() {
  local kind="$1"
  local index="$2"
  local spec_output_path="$3"
  shift 3
  local cmd=("$@")

  local kind_lower
  kind_lower="$(to_lower "$kind")"
  local stdout_file="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}-${kind_lower}-${index}.stdout.json"
  local stderr_file="$SEED_LOG_DIR/seed-${SEED_TIMESTAMP}-${kind_lower}-${index}.stderr.log"
  local command_text
  command_text="$(printf "%q " "${cmd[@]}")"

  set +e
  "${cmd[@]}" > "$stdout_file" 2> "$stderr_file"
  local exit_code=$?
  set -e

  local run_id
  run_id="$(json_value "$stdout_file" "runId")"
  local chain_failures
  chain_failures="$(json_value "$stdout_file" "data.summary.chainFailures")"
  if [[ -z "$chain_failures" ]]; then
    chain_failures="0"
  fi

  local code_run_id=""
  local doc_run_id=""
  local review_run_id=""
  local chain_payload=""

  if [[ "$kind" == "CHAIN" && -n "$run_id" ]]; then
    chain_payload="$(extract_chain_mapping "$run_id")"
    code_run_id="$(python3 - "$chain_payload" <<'PY'
import json
import sys
payload = json.loads(sys.argv[1]) if sys.argv[1].strip() else {}
print(payload.get("codeRunId", ""))
PY
)"
    doc_run_id="$(python3 - "$chain_payload" <<'PY'
import json
import sys
payload = json.loads(sys.argv[1]) if sys.argv[1].strip() else {}
print(payload.get("docRunId", ""))
PY
)"
    review_run_id="$(python3 - "$chain_payload" <<'PY'
import json
import sys
payload = json.loads(sys.argv[1]) if sys.argv[1].strip() else {}
print(payload.get("reviewRunId", ""))
PY
)"
  fi

  append_record "$kind" "$index" "$exit_code" "$run_id" "$chain_failures" "$spec_output_path" "$code_run_id" "$doc_run_id" "$review_run_id" "$stdout_file" "$stderr_file" "$command_text" "$chain_payload"

  run_count=$((run_count + 1))
  local chain_signal="off"
  if [[ "$kind" == "CHAIN" ]]; then
    chain_signal="on"
  fi
  log "$kind#$index exit=$exit_code runId=${run_id:-N/A} chain=$chain_signal chainFailures=$chain_failures"

  if [[ "$kind" == "CHAIN" ]]; then
    log "$kind#$index mapping specRunId=${run_id:-N/A} codeRunId=${code_run_id:-N/A} docRunId=${doc_run_id:-N/A} reviewRunId=${review_run_id:-N/A}"
  fi

  if [[ $exit_code -ne 0 || -z "$run_id" ]]; then
    failure_count=$((failure_count + 1))
    if as_bool "$SEED_FAIL_FAST"; then
      local fail_fast_exit_code="$exit_code"
      if [[ $fail_fast_exit_code -eq 0 && -z "$run_id" ]]; then
        fail_fast_exit_code=1
      fi
      log "중단: fail-fast 활성화 상태에서 실패 발생(kind=$kind index=$index exit=$fail_fast_exit_code)"
      exit "$fail_fast_exit_code"
    fi
  fi
}

if (( SEED_DIRECT_RUNS > 0 )); then
  for ((i=1; i<=SEED_DIRECT_RUNS; i++)); do
    request_text="${SEED_DIRECT_REQUEST_PREFIX} #${i} (${SEED_TIMESTAMP})"
    run_single "DIRECT" "$i" "" \
      ./devagent generate \
      -u "$request_text" \
      --chain-to-doc false \
      --chain-to-review false \
      -a "$SEED_APPLY" \
      -m "$SEED_MODE" \
      --json=true
  done
fi

if (( SEED_CHAIN_RUNS > 0 )); then
  for ((i=1; i<=SEED_CHAIN_RUNS; i++)); do
    spec_output_path="$SEED_SPEC_OUTPUT_DIR/seed-${SEED_TIMESTAMP}-chain-${i}.json"
    spec_request="${SEED_CHAIN_SPEC_REQUEST_PREFIX} #${i} (${SEED_TIMESTAMP})"
    code_request="${SEED_CHAIN_CODE_REQUEST_PREFIX} #${i} (${SEED_TIMESTAMP})"
    doc_request="${SEED_CHAIN_DOC_REQUEST_PREFIX} #${i} (${SEED_TIMESTAMP})"
    review_request="${SEED_CHAIN_REVIEW_REQUEST_PREFIX} #${i} (${SEED_TIMESTAMP})"

    run_single "CHAIN" "$i" "$spec_output_path" \
      ./devagent spec \
      -u "$spec_request" \
      --chain-to-code true \
      --code-user-request "$code_request" \
      --code-chain-to-doc true \
      --code-doc-user-request "$doc_request" \
      --code-chain-to-review true \
      --code-review-user-request "$review_request" \
      --code-chain-failure-policy PARTIAL_SUCCESS \
      --spec-output-path "$spec_output_path" \
      -m "$SEED_MODE" \
      --json=true
  done
fi

snapshot_run_state "$SNAPSHOT_AFTER"
build_summary

log "run-state snapshot(after): $SNAPSHOT_AFTER"
log "요약: total=$run_count, failures=$failure_count"
log "산출물:"
log "- log: $RUN_LOG"
log "- records: $RUN_RECORDS"
log "- before: $SNAPSHOT_BEFORE"
log "- after: $SNAPSHOT_AFTER"
log "- summary: $SUMMARY_JSON"

if (( failure_count > 0 )); then
  exit 1
fi

exit 0
