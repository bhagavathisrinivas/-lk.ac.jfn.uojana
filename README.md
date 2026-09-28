# UoJ Accessibility Needs Assessment - Firebase + Python + Android Studio

    uoj_ana/
    ├── backend/    Python (Firebase Admin SDK): schema, seeding, roles, Excel export
    ├── firebase/   firestore.rules + firebase.json
    └── android/    Android Studio project (Kotlin + Jetpack Compose)

How it fits together: the questionnaire lives in `backend/questionnaire_schema.py`,
is uploaded to Firestore, and the Android app renders it dynamically. Answers are written to
`responses/` (write-once), and Python exports them to Excel.

## 1. Firebase console
1. Create a project, add an Android app with package name `lk.ac.jfn.uojana`.
2. Download `google-services.json` -> put in `android/app/`.
3. Build > Authentication > enable **Email/Password**.
4. Build > Firestore Database > create (production mode).
5. Project settings > Service accounts > **Generate new private key** -> save as
   `backend/serviceAccount.json` (NEVER commit or share this file).

## 2. Deploy rules
    npm i -g firebase-tools
    cd firebase && firebase login && firebase use --add && firebase deploy --only firestore:rules

## 3. Python backend
    cd backend
    pip install -r requirements.txt
    python questionnaire_schema.py                      # validates (62 questions)
    python seed_schema.py --key serviceAccount.json     # upload questionnaire
    python set_role.py --key serviceAccount.json --email you@jfn.ac.lk --role staff
    python export_responses.py --key serviceAccount.json --out responses.xlsx
    python export_responses.py --key serviceAccount.json --anonymise --out anon.xlsx

## 4. Android
Open `android/` in Android Studio, let Gradle sync, run on an emulator/device.

## Notes
- Editing questions: change the schema, bump `VERSION`, re-run `seed_schema.py`. No app update needed.
- Replace the consent wording with text approved by the university's ethics/legal office.
- Add Tamil/Sinhala by adding translated labels to the schema (or a second questionnaire doc).
- Data is sensitive (disability, income, contact details): keep the service-account key private,
  restrict staff roles, and use `--anonymise` for sharing.
