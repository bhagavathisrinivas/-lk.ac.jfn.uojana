"""
Export all responses to Excel with a summary sheet.
  python export_responses.py --key serviceAccount.json --out responses.xlsx
  python export_responses.py --key serviceAccount.json --anonymise   # drop name/phone/email/DOB
"""
import argparse
import firebase_admin
import pandas as pd
from firebase_admin import credentials, firestore
from questionnaire_schema import SECTIONS

PERSONAL = ["full_name", "dob", "phone", "email"]


def flatten(value):
    return "; ".join(value) if isinstance(value, list) else value


def load_rows(db):
    rows = []
    for d in db.collection("responses").stream():
        r = d.to_dict()
        row = {"response_id": d.id, "submitted_at": r.get("submittedAt"),
               "questionnaire_version": r.get("version")}
        for k, v in (r.get("answers") or {}).items():
            row[k] = flatten(v)
        rows.append(row)
    return rows


def summary(df):
    parts = []
    for col in ["faculty", "level", "income", "urgency", "overall_impact", "uses_at"]:
        if col in df:
            t = df[col].value_counts().rename_axis("answer").reset_index(name="count")
            t.insert(0, "question", col)
            parts.append(t)
    if "disability_types" in df:
        t = (df["disability_types"].dropna().str.split("; ").explode()
             .value_counts().rename_axis("answer").reset_index(name="count"))
        t.insert(0, "question", "disability_types")
        parts.append(t)
    return pd.concat(parts) if parts else pd.DataFrame()


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--key", required=True)
    p.add_argument("--out", default="responses.xlsx")
    p.add_argument("--anonymise", action="store_true")
    a = p.parse_args()

    firebase_admin.initialize_app(credentials.Certificate(a.key))
    rows = load_rows(firestore.client())
    if not rows:
        raise SystemExit("No responses yet.")

    order = [x["id"] for s in SECTIONS for x in s["questions"]]
    df = pd.DataFrame(rows)
    df["submitted_at"] = pd.to_datetime(df["submitted_at"], utc=True).dt.tz_localize(None)
    if a.anonymise:
        df = df.drop(columns=[c for c in PERSONAL if c in df])
    cols = ["response_id", "submitted_at", "questionnaire_version"] + [c for c in order if c in df]
    df = df[cols]

    with pd.ExcelWriter(a.out, engine="openpyxl") as w:
        df.to_excel(w, sheet_name="Responses", index=False)
        summary(df).to_excel(w, sheet_name="Summary", index=False)
    print(f"Wrote {len(df)} responses to {a.out}")
