import os
import re

def process_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        changed = False

        # Add @Shadow private boolean dirty; if not present
        if 'private boolean dirty;' not in content:
            shadow_pattern_legacy = r'(@Shadow\s+private String getCurrentPageContent\(\) \{)'
            shadow_pattern_modern = r'(@Shadow\s+private void updatePageContent\(\) \{)'
            shadow_pattern_modern2 = r'(@Shadow\s+private void updatePage\(\) \{)'
            
            if re.search(shadow_pattern_legacy, content):
                content = re.sub(shadow_pattern_legacy, r'@Shadow\n    private boolean dirty;\n\n    \1', content)
            elif re.search(shadow_pattern_modern, content):
                content = re.sub(shadow_pattern_modern, r'@Shadow\n    private boolean dirty;\n\n    \1', content)
            elif re.search(shadow_pattern_modern2, content):
                content = re.sub(shadow_pattern_modern2, r'@Shadow\n    private boolean dirty;\n\n    \1', content)
        
        content_old = content
        
        # Legacy: Add this.dirty = true; after setPageContent
        content = re.sub(r'(this\.setPageContent\(workingPages\.get\(this\.currentPage\)\);)(?!\s*this\.dirty = true;)', r'\1\n            this.dirty = true;', content)
        content = re.sub(r'(this\.setPageContent\(this\.pages\.get\(this\.currentPage\)\);)(?!\s*this\.dirty = true;)', r'\1\n        this.dirty = true;', content)
        
        # Modern: Add this.dirty = true; after updatePageContent or updatePage
        content = re.sub(r'(this\.updatePageContent\(\);)(?!\s*this\.dirty = true;)', r'\1\n            this.dirty = true;', content)
        content = re.sub(r'(this\.updatePage\(\);)(?!\s*this\.dirty = true;)', r'\1\n            this.dirty = true;', content)
        
        # Modern in bookpaste$setPagesAndCurrentPage (indent is 8 spaces)
        content = re.sub(r'(this\.updatePageContent\(\);\n\s*this\.updateButtonVisibility\(\);\n\s*this\.saveChanges\(\);)(?!\s*this\.dirty = true;)', r'\1\n        this.dirty = true;', content)
        content = re.sub(r'(this\.updatePage\(\);\n\s*this\.updatePreviousPageButtonVisibility\(\);)(?!\s*this\.dirty = true;)', r'\1\n        this.dirty = true;', content)
        
        # Actually a safer way for Modern:
        content = re.sub(r'(this\.saveChanges\(\);)(?!\s*this\.dirty = true;)', r'\1\n            this.dirty = true;', content)
        
        if content != content_old:
            changed = True

        if changed:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Patched {filepath}")

    except Exception as e:
        print(f"Error processing {filepath}: {e}")

# Find all mixin files
for root, dirs, files in os.walk('d:/Projects/bookpaste/versions'):
    for file in files:
        if file.startswith('BookEditScreen') and file.endswith('Mixin.java'):
            filepath = os.path.join(root, file)
            process_file(filepath)

print("Done")
