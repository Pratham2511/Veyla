import sys

paths = [
    '/home/z/my-project/android/app/src/main/java/com/pratham/webhub/ui/addtab/AddTabSheet.kt',
    '/home/z/my-project/android/app/src/main/java/com/pratham/webhub/ui/bookmarks/BookmarksScreen.kt',
]

for path in paths:
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace(
        'import androidx.compose.runtime.collectAsStateWithLifecycle',
        'import androidx.lifecycle.compose.collectAsStateWithLifecycle'
    )
    with open(path, 'w') as f:
        f.write(content)
    print(f'Fixed {path}')
