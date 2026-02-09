import re, sys

file_path = r'D:\VocalCoach\app\src\main\java\com\vocalcoach\app\data\local\AppDatabase.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

total_quotes = content.count('"')
print(f"Original: {len(content)} chars, {total_quotes} quotes")

def fix_embedded_quotes(match):
    prefix = match.group(1)
    desc = match.group(2)
    suffix = match.group(3)
    fixed = ''
    opening = True
    for ch in desc:
        if ch == '"':
            fixed += '\u300c' if opening else '\u300d'
            opening = not opening
        else:
            fixed += ch
    return prefix + fixed + suffix

# Fix DailyTask descriptions: "title", "description with quotes", TaskType.XXX
content = re.sub(
    r'(DailyTask\(\d+,\s*\d+,\s*\d+,\s*"[^"]*",\s*")(.+?)(",\s*TaskType\.\w+)',
    fix_embedded_quotes,
    content
)

# Fix Lesson descriptions: "title", "description with quotes", LessonCategory.XXX
content = re.sub(
    r'(Lesson\(\d+,\s*"[^"]*",\s*")(.+?)(",\s*LessonCategory\.\w+)',
    fix_embedded_quotes,
    content
)

new_quotes = content.count('"')
brackets = content.count('\u300c') + content.count('\u300d')
print(f"After fix: {len(content)} chars, {new_quotes} quotes, {brackets} brackets added")

# Verify no broken lines
lines = content.split('\n')
ok = True
for i, line in enumerate(lines, 1):
    qc = line.count('"')
    if 'DailyTask(' in line and qc != 4:
        print(f"PROBLEM line {i}: DailyTask has {qc} quotes (expected 4)")
        ok = False
    if 'Lesson(' in line and 'thumbnailEmoji' in line and qc != 6:
        print(f"PROBLEM line {i}: Lesson has {qc} quotes (expected 6)")
        ok = False
    if 'Achievement(' in line and 'AchievementCategory' in line and qc != 6:
        print(f"PROBLEM line {i}: Achievement has {qc} quotes (expected 6)")
        ok = False

if ok:
    with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print("All checks passed. File saved!")
else:
    print("Issues found. File NOT saved.")
    sys.exit(1)
