import json
import urllib.request
import urllib.error
from pathlib import Path

BASE = "http://127.0.0.1:8080/api"
OUT = Path(__file__).with_name("api-crud-evidence.json")
evidence = {"steps": []}

def call(method, path, body=None):
    data = None if body is None else json.dumps(body, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        BASE + path,
        data=data,
        method=method,
        headers={"Content-Type": "application/json; charset=utf-8"},
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        return exc.code, exc.read().decode("utf-8", "replace")

def record(label, method, path, body=None):
    status, response = call(method, path, body)
    evidence["steps"].append({"label": label, "method": method, "path": path, "status": status, "response": response})
    print(label, status, json.dumps(response, ensure_ascii=False)[:260])
    return status, response

def payload(name, phone, address):
    return {"receiverName": name, "phone": phone, "fullAddress": address, "lineNo": 1}

record("health", "GET", "/health")
record("health_db", "GET", "/health/db")

ids = {}
for n, phone in [("MSDEMOCRUDAA", "17790003001"), ("MSDEMOCRUDAB", "17790003002"), ("MSDEMOCRUDAC", "17790003003")]:
    status, r = record("create-" + n, "POST", "/addresses", payload(n, phone, "Zhejiang Hangzhou Xihu MS10-DEMO-CRUD-" + n))
    ids[n] = int(r["data"]["id"])

record("list-page-1-size-2", "GET", "/addresses?page=1&size=2")
record("search-name", "GET", "/addresses?page=1&size=10&keyword=MSDEMOCRUDAA")
record("search-phone", "GET", "/addresses?page=1&size=10&keyword=17790003002")

record("get-before-edit", "GET", f"/addresses/{ids['MSDEMOCRUDAA']}")
record("edit-01", "PUT", f"/addresses/{ids['MSDEMOCRUDAA']}", payload("MSDEMOCRUDAA", "17790003001", "Zhejiang Hangzhou Xihu MS10-DEMO-CRUD-EDITED"))
record("get-after-edit", "GET", f"/addresses/{ids['MSDEMOCRUDAA']}")

record("delete-03", "DELETE", f"/addresses/{ids['MSDEMOCRUDAC']}")
record("get-deleted-03", "GET", f"/addresses/{ids['MSDEMOCRUDAC']}")

batch_ids = []
for n, phone in [("MSDEMOCRUDAD", "17790003004"), ("MSDEMOCRUDAAE", "17790003005")]:
    status, r = record("create-" + n, "POST", "/addresses", payload(n, phone, "Zhejiang Hangzhou Xihu MS10-DEMO-CRUD-" + n))
    batch_ids.append(str(r["data"]["id"]))
record("batch-delete", "POST", "/addresses/batch-delete", {"ids": batch_ids})
record("list-after-crud", "GET", "/addresses?page=1&size=10&keyword=MSDEMO")

evidence["ids"] = ids
evidence["batch_ids"] = batch_ids
OUT.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")
print("evidence", OUT)
