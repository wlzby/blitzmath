import codecs
file_path = 'app/src/main/java/com/mawelly/blitzmath/localization/Strings.kt'
with codecs.open(file_path, 'r', 'utf-8') as f:
    lines = f.readlines()

replacement = [
    '            AppLanguage.CHINESE -> \"?????????!g???? ????????????\"\\n',
    '            AppLanguage.RUSSIAN -> \"????????? ??????????\"\\n',
    '        }\\n'
]
lines[1587:1690] = replacement

with codecs.open(file_path, 'w', 'utf-8') as f:
    f.writelines(lines)
print('Done!')
