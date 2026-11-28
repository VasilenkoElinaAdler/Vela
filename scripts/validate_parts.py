"""Fail the build if a required lesson term or local asset is absent."""
import json,re,zipfile
from pathlib import Path
root=Path(__file__).resolve().parents[1]
spec=json.loads((root/'docs/parts_curriculum.json').read_text())
logic=(root/'app/src/main/java/com/velaelina/cards/PartsLogic.kt').read_text()
found=dict(re.findall(r'([A-Z_]+)\("([^"]+)","[^"]+"\)',logic))
assert set(found.values())==set(spec['terms']), (found,spec['terms'])
zpath=root/'app/src/main/assets/vela_parts_assets_v2.zip'
assert zpath.is_file(), f'Missing {zpath.name}; upload the approved asset pack before building'
with zipfile.ZipFile(zpath) as z:
    assert z.testzip() is None
    assert set(z.namelist())==set(spec['assets'])
    for item in z.infolist():
        assert 0<item.file_size<=1_000_000, item.filename
        content=z.read(item.filename)
        if item.filename.endswith('.ogg'): assert content.startswith(b'OggS'), item.filename
        if item.filename.endswith('.webp'): assert content[:4]==b'RIFF' and content[8:12]==b'WEBP',item.filename
print('Seven approved terms and all fourteen local assets verified.')
