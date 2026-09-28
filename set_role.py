"""Give a user a role: student (default) | staff | admin. Staff/admin can read all responses."""
import argparse
import firebase_admin
from firebase_admin import auth, credentials

p = argparse.ArgumentParser()
p.add_argument("--key", required=True)
p.add_argument("--email", required=True)
p.add_argument("--role", choices=["student", "staff", "admin"], required=True)
a = p.parse_args()

firebase_admin.initialize_app(credentials.Certificate(a.key))
user = auth.get_user_by_email(a.email)
auth.set_custom_user_claims(user.uid, {"role": a.role})
print(f"{a.email} is now '{a.role}'. They must sign out and in again.")
