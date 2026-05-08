import os
import json

def fix_mojibake(val):
    if isinstance(val, str):
        try:
            # Try to encode as cp1251 and decode as utf-8
            fixed = val.encode('cp1251').decode('utf-8')
            return fixed
        except (UnicodeEncodeError, UnicodeDecodeError):
            return val
    elif isinstance(val, dict):
        return {k: fix_mojibake(v) for k, v in val.items()}
    elif isinstance(val, list):
        return [fix_mojibake(v) for v in val]
    return val

def process_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8-sig') as f:
            data = json.load(f)
        
        new_data = fix_mojibake(data)
        
        # Check if anything changed to avoid rewriting unnecessarily
        if data != new_data:
            with open(filepath, 'w', encoding='utf-8-sig') as f:
                json.dump(new_data, f, ensure_ascii=False, indent=2)
                f.write('\n') # add trailing newline
            print(f"Fixed {filepath}")
    except Exception as e:
        print(f"Error processing {filepath}: {e}")

# Find all json files
for root, dirs, files in os.walk('d:/Projects/bookpaste'):
    if 'build' in root or '.gradle' in root or '.git' in root: continue
    for file in files:
        if file.endswith('.json'):
            filepath = os.path.join(root, file)
            process_file(filepath)

print("Done")
