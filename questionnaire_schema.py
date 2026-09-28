"""
UoJ Accessibility Needs Assessment - questionnaire defined as DATA.

The Android app renders whatever is stored in Firestore, so you can change
wording/options here and re-run seed_schema.py without publishing a new APK
(bump VERSION whenever you change it).

Question types: text | long_text | number | single | multi
Optional keys : hint, required, show_if={"id": <earlier question id>, "in": [values]},
                must_equal=<value the answer must be>
"""

QUESTIONNAIRE_ID = "uoj_ana"
VERSION = "1.2"

DIFF = ["No difficulty", "Some difficulty", "A lot of difficulty", "Cannot do at all"]
ACCESS = ["Not Accessible", "Partially Accessible", "Accessible to use", "Accessible as per Compliance"]
PLATFORM = ["Completely Accessible", "Partially Accessible", "Inaccessible", "Do Not Use"]
USES_AT_YES = ["Yes", "I have used assistive technology previously"]


def q(id, type, label, options=None, required=False, hint="", show_if=None, must_equal=None):
    d = {"id": id, "type": type, "label": label, "required": required}
    if hint:
        d["hint"] = hint
    if options:
        d["options"] = options
    if show_if:
        d["show_if"] = show_if
    if must_equal:
        d["must_equal"] = must_equal
    return d


def when(qid, values):
    return {"id": qid, "in": values}


SECTIONS = [
    {"id": "s0", "title": "Consent", "questions": [
        q("consent", "single",
          "I understand that my answers are confidential, will be used by the Disability Support "
          "Center to plan support services, and that taking part is voluntary.",
          ["I agree", "I do not agree"], True, must_equal="I agree",
          hint="Replace this text with wording approved by the university."),
    ]},

    {"id": "s1", "title": "Admission & Basic Information", "questions": [
        q("full_name", "text", "Full name", required=True),
        q("age", "number", "Age", required=True),
        q("dob", "text", "Date of birth (DD/MM/YYYY)", required=True),
        q("gender", "single", "Gender", ["Male", "Female", "Other"], True),
        q("faculty", "single", "Faculty & Department",
          ["Arts", "Science", "Agriculture", "Medicine", "Engineering",
           "Management Studies & Commerce", "Technology", "Graduate Studies",
           "Siddha Medicine", "Ramanathan Academy of Fine Arts", "Other"], True),
        q("campus", "text", "Study campus / location"),
        q("programme", "text", "Degree programme / course of study", required=True),
        q("admission_year", "text", "Year of admission"),
        q("level", "single", "Current academic level",
          ["Undergraduate - Year 1", "Undergraduate - Year 2", "Undergraduate - Year 3",
           "Undergraduate - Year 4", "Postgraduate - Master's", "Postgraduate - MPhil",
           "Postgraduate - PhD", "Diploma", "Certificate"], True),
        q("phone", "text", "Contact phone number"),
        q("email", "text", "University / personal email"),
    ]},

    {"id": "s2", "title": "Disability & Functional Profile", "questions": [
        q("disability_types", "multi", "Nature of your primary disability / condition",
          ["Visual Impairment (Total Blindness / Low Vision)",
           "Hearing Impairment (Deafness / Hard of Hearing)",
           "Speech and Language / Communication Disability",
           "Physical / Locomotor Disability / Mobility Impairment",
           "Intellectual Disability",
           "Specific Learning Disability (Dyslexia, Dysgraphia, Dyscalculia, etc.)",
           "Autism Spectrum Condition / Neurodevelopmental Condition",
           "Mental Health Condition / Psychosocial Disability",
           "Chronic Illness / Medical Condition affecting functioning",
           "Multiple Disabilities", "Other"], True, hint="Select all that apply."),
        q("disability_other", "text", "Other (please specify)",
          show_if=when("disability_types", ["Other"])),
        q("documentation", "single",
          "Do you have official disability documentation or a medical certificate "
          "in accordance with Sri Lankan national guidelines?",
          ["Yes", "No", "In the process of obtaining"], True),
        q("wg_seeing", "single", "Seeing, even if wearing glasses", DIFF, True),
        q("wg_hearing", "single", "Hearing, even if using a hearing aid", DIFF, True),
        q("wg_mobility", "single", "Walking or climbing steps / physical mobility", DIFF, True),
        q("wg_cognition", "single", "Remembering or concentrating", DIFF, True),
        q("wg_selfcare", "single", "Self-care (washing, dressing, hygiene management)", DIFF, True),
        q("wg_communication", "single",
          "Communicating (understanding or being understood in your usual language)", DIFF, True),
        q("wg_upper_body", "single",
          "Upper body movement / using hands or fingers for classroom tasks", DIFF, True),
        q("overall_impact", "single", "Overall impact of disability on university life",
          ["Not at all", "Slightly", "Moderately", "Significantly", "Very Significantly"], True),
        q("functional_desc", "long_text",
          "Describe any specific functional difficulties affecting your university participation"),
    ]},

    {"id": "s3", "title": "Economic Background & Access to Assistive Technology", "questions": [
        q("income", "single", "Approximate total monthly household income (LKR)",
          ["Below LKR 25,000", "LKR 25,001 - 50,000", "LKR 50,001 - 75,000",
           "LKR 75,001 - 100,000", "Above LKR 100,000"], True),
        q("support_source", "multi", "Main source of financial support while studying",
          ["Parent / Family", "Personal income", "Mahapola / Government scholarship",
           "University financial aid", "Disability allowance / Government social assistance",
           "Educational loan", "NGO / Charitable organization", "Relatives / Friends", "Other"],
          True, hint="Select all that apply."),
        q("support_other", "text", "Other (please specify)", show_if=when("support_source", ["Other"])),
        q("contribution", "single",
          "Financial contribution you or your family could reasonably make towards required AT",
          ["Unable to contribute financially / Require 100% subsidy", "Below LKR 10,000",
           "LKR 10,001 - 25,000", "LKR 25,001 - 50,000", "LKR 50,001 - 100,000",
           "LKR 100,001 - 250,000", "Above LKR 250,000", "Not sure"], True),
    ]},

    {"id": "s4", "title": "Current Use of Assistive Technology", "questions": [
        q("uses_at", "single", "Do you currently use any assistive technology?",
          ["Yes", "No", "I have used assistive technology previously", "Not sure"], True),
        q("at_used", "multi", "Which assistive technologies do you use or have used?",
          ["Screen reader (JAWS, NVDA, TalkBack, VoiceOver)",
           "Screen magnification software / Electronic magnifier",
           "Handheld / Optical magnifier",
           "Refreshable Braille display / Braille notetaker / Perkins Brailler / Braille materials",
           "OCR / Text recognition / Text-to-speech software",
           "Smartphone accessibility features / AI tools",
           "Hearing aid / Cochlear implant",
           "Assistive listening device / FM system",
           "Real-time captioning / Speech-to-text software",
           "AAC device / software / apps",
           "Manual / Powered / Reclining or Transfer wheelchair",
           "Walker / Crutches / Walking stick / Transfer boards",
           "Prosthesis / Orthosis / Adaptive seating",
           "Adaptive keyboard / Adaptive mouse / Alternative input device",
           "Speech recognition / Voice typing software", "Other"],
          True, show_if=when("uses_at", USES_AT_YES), hint="Select all that apply."),
        q("at_provider", "multi", "Who provided or paid for your assistive technology?",
          ["Self / Parents / Family", "University / Government programme",
           "NGO / Charitable organization / Donation", "Borrowed / Loaned device", "Other"],
          show_if=when("uses_at", USES_AT_YES)),
        q("at_adequate", "single", "Is your current assistive technology adequate for your educational needs?",
          ["Completely adequate", "Mostly adequate", "Partially adequate",
           "Inadequate / Completely inadequate", "Not applicable"],
          show_if=when("uses_at", USES_AT_YES)),
        q("at_difficulties", "multi", "Difficulties you experience with your existing assistive technology",
          ["Device is old / outdated / does not fully meet needs",
           "High maintenance cost / expensive software licence",
           "Repair facilities or spare parts unavailable",
           "Lack of training / lack of technical support",
           "Not compatible with university websites / LMS / digital materials / exam systems",
           "Internet / connectivity / battery charging difficulties",
           "No significant difficulty", "Other"],
          show_if=when("uses_at", USES_AT_YES)),
    ]},

    {"id": "s5", "title": "Assistive Technology Needs & Support Services", "questions": [
        q("needed_at", "long_text",
          "AT device, software or service you need but do not have. State your highest priority."),
        q("need_use", "multi", "What would you use this technology for?",
          ["Attending lectures / reading textbooks / accessing digital materials",
           "Writing / note-taking / assignments / research",
           "Examinations / presentations / laboratory and practical activities",
           "Communication / library use / computer use",
           "Mobility within university / travelling / independent living",
           "Extracurricular activities / other"]),
        q("why_no_access", "multi", "Why do you not currently have access to it?",
          ["Cannot afford it (self or family)", "Device not available in Sri Lanka",
           "Do not know where to obtain it or which technology is appropriate",
           "Need professional assessment before selecting",
           "University does not provide it / unaware of government schemes",
           "Applied for assistance and awaiting response",
           "Lack of training / repair and technical support", "Other"]),
        q("urgency", "single", "How urgently do you require this assistive technology?",
          ["Immediately", "Within 3 months", "Within 6 months", "Within one year / Not urgent"], True),
        q("help_type", "multi", "What assistance would help you obtain the required AT?",
          ["Full or partial financial assistance / Government subsidy / University / NGO donation",
           "Device lending / loan programme", "Professional assessment and recommendation",
           "Assistance purchasing / importing device", "Training", "Technical and repair support"]),
        q("assessment_received", "single", "Have you ever received a professional AT assessment?",
          ["Yes", "No", "Not sure"]),
        q("assessment_want", "single", "Would you like to receive a professional AT assessment?",
          ["Yes", "No", "Maybe"]),
        q("services_useful", "multi", "Which AT-related services would be useful to you?",
          ["Assessment of AT needs", "Advice on selecting appropriate AT", "Trial devices before obtaining",
           "AT training", "Technical support", "Repair and maintenance services",
           "Software installation & configuration", "Accessibility settings assistance",
           "Information on government support schemes & application assistance",
           "Device loan / lending service", "Other"]),
    ]},

    {"id": "s6", "title": "Teaching, Learning & Examination Accommodations", "questions": [
        q("barrier_freq", "single", "Do you experience accessibility barriers during teaching, learning or lectures?",
          ["Never", "Rarely", "Sometimes", "Often", "Always"], True),
        q("formats_needed", "multi", "Accessible teaching and learning formats / supports you require",
          ["Accessible electronic documents (screen-reader compatible PDF / Word / HTML)",
           "Braille", "Large print materials", "Audio materials", "Lecture recordings",
           "Captioned video", "Sign language interpretation", "Real-time captioning or speech-to-text",
           "Accessible PowerPoint presentations", "Note-taking assistance", "Scribe or reader support"]),
        q("plat_lms", "single", "Accessibility: University Learning Management System (LMS / VLE)", PLATFORM),
        q("plat_sis", "single", "Accessibility: Student Information System and Registration Portal", PLATFORM),
        q("plat_web", "single", "Accessibility: Main University Website and Faculty Websites", PLATFORM),
        q("plat_lib", "single", "Accessibility: Digital Library Repository and OPAC", PLATFORM),
        q("plat_exam", "single", "Accessibility: Online Examination / Assessment Platforms", PLATFORM),
        q("exam_accom", "multi", "Examination / assessment accommodations you require",
          ["Additional time", "Rest breaks", "Flexible scheduling",
           "Scribe / writer / reader / sign language interpreter",
           "Separate or physically accessible examination room",
           "Computer / screen reader / magnification / assistive technology",
           "Braille / large-print / electronic examination papers",
           "Alternative method of answering or alternative assessment format"]),
        q("exam_prev", "single",
          "Have you previously requested exam accommodations, and were they provided satisfactorily?",
          ["Fully provided", "Partially provided", "Not provided", "Provided after delay",
           "Did not know I could request"]),
    ]},

    {"id": "s7", "title": "Physical Campus Infrastructure & Residential Accessibility", "questions": [
        q("infra_" + k, "single", label, ACCESS)
        for k, label in [
            ("entrance", "University entrance"), ("parking", "Parking areas"),
            ("pathways", "Pathways"), ("pavements", "Pavements"),
            ("bldg_entrances", "Building entrances"), ("doorways", "Doorways"),
            ("corridors", "Corridors"), ("stairs", "Stairs and handrails"),
            ("ramps", "Ramps (if available)"), ("lifts", "Lifts (if available)"),
            ("classrooms", "Classrooms and lecture halls"), ("labs", "Laboratories"),
        ]
    ]},
    # Add further Section 7 items (restrooms, library, hostels ...) here.
]

QUESTIONNAIRE = {
    "title": "Students with Disabilities - Accessibility Needs Assessment",
    "version": VERSION,
    "sections": SECTIONS,
}


def validate(data=QUESTIONNAIRE):
    seen = set()
    for s in data["sections"]:
        for x in s["questions"]:
            assert x["id"] not in seen, f"duplicate id {x['id']}"
            assert x["type"] in {"text", "long_text", "number", "single", "multi"}, x["id"]
            if x["type"] in {"single", "multi"}:
                assert x.get("options"), f"{x['id']} needs options"
            si = x.get("show_if")
            if si:
                assert si["id"] in seen, f"{x['id']} show_if must reference an earlier question"
            seen.add(x["id"])
    return len(seen)


if __name__ == "__main__":
    print("OK -", validate(), "questions")
