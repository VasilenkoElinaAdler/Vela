"""Independent curriculum and archive checks for both image missions."""
import json,re,zipfile
from pathlib import Path
root=Path(__file__).resolve().parents[1]
spec=json.loads((root/'docs/parts_curriculum.json').read_text())
logic=(root/'app/src/main/java/com/velaelina/cards/PartsLogic.kt').read_text()
found=dict(re.findall(r'([A-Z_]+)\("([^"]+)","[^"]+"\)',logic))
assert set(found.values())==set(spec['terms'])|set(spec['side']['terms']), found
for name,terms in [('TOP',spec['terms']),('SIDE',spec['side']['terms'])]:
    line=next(l for l in logic.splitlines() if l.strip().startswith(name+'("'))
    actual=[found[x] for x in re.findall(r'BoatPart\.([A-Z_]+)',line)]
    assert set(actual)==set(terms) and len(actual)==len(terms),(name,actual,terms)
for archive,entries in [('vela_parts_assets_v2.zip',spec['assets']),('vela_parts_side_assets.zip',spec['side']['assets'])]:
    zpath=root/'app/src/main/assets'/archive
    assert zpath.is_file(),f'Missing {archive}; upload approved pack before building'
    with zipfile.ZipFile(zpath) as z:
        assert z.testzip() is None
        assert set(z.namelist())==set(entries)
        for item in z.infolist():
            assert 0<item.file_size<=1_000_000,item.filename
            content=z.read(item.filename)
            if item.filename.endswith('.ogg'):assert content.startswith(b'OggS'),item.filename
            if item.filename.endswith('.webp'):assert content[:4]==b'RIFF' and content[8:12]==b'WEBP',item.filename
print('Both approved mission catalogues and all nineteen local assets verified.')
