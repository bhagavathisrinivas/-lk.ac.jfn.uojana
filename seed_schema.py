"""Upload the questionnaire to Firestore (collection: questionnaires)."""
import argparse
import firebase_admin
from firebase_admin import credentials, firestore
from questionnaire_schema import QUESTIONNAIRE, QUESTIONNAIRE_ID, validate

p = argparse.ArgumentParser()
p.add_argument("--key", required=True, help="path to service-account JSON")
a = p.parse_args()

print("Validated", validate(), "questions")
firebase_admin.initialize_app(credentials.Certificate(a.key))
db = firestore.client()
db.collection("questionnaires").document(QUESTIONNAIRE_ID).set(QUESTIONNAIRE)
print(f"Uploaded questionnaires/{QUESTIONNAIRE_ID} (v{QUESTIONNAIRE['version']})")
