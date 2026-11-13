#!/usr/bin/env python3
"""Run after committing a signed APK; commit generated metadata and push main."""
import hashlib
import json
import re
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parent.parent

def git(*args):
    return subprocess.check_output(['git', '-C', str(root), *args])

commit = git('rev-parse', 'HEAD').decode().strip()
build = git('show', 'HEAD:app/build.gradle.kts').decode()
code = int(re.search(r'versionCode\s*=\s*(\d+)', build)[1])
name = re.search(r'versionName\s*=\s*"([\d.]+)"', build)[1]
path = f'downloads/Sizzle-{name}.apk'
apk = git('show', f'HEAD:{path}')
metadata = {
    'versionCode': code,
    'versionName': name,
    'downloadUrl': f'https://raw.githubusercontent.com/dozyFJ/Sizzle-/{commit}/{path}',
    'sha256': hashlib.sha256(apk).hexdigest(),
}
output = root / 'updates/latest.json'
output.parent.mkdir(exist_ok=True)
output.write_text(json.dumps(metadata, indent=2) + '\n')
print(f'Prepared update {name} from commit {commit}')
