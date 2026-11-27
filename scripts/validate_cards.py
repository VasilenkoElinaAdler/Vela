from pathlib import Path
import json, re
from collections import Counter
root=Path(__file__).resolve().parents[1]/'app/src/main/assets/cards'
base=None
for file in sorted(root.glob('*.json')):
    cards=json.loads(file.read_text(encoding='utf-8'))
    assert cards, file
    ids=set(); questions=set()
    for c in cards:
        id=c['id']; assert re.fullmatch(r'VELA-\d{3,}',id) and id not in ids, id
        ids.add(id)
        q=c['question'].strip(); assert q and q not in questions, id
        questions.add(q)
        assert c['category'] in ('equipment','maneuver','course','physics','practice','rules'),id
        assert c['difficulty'] in (1,2,3),id
        opts=c['options']; assert len(opts)==4 and len(set(o.strip().casefold() for o in opts))==4 and all(o.strip() for o in opts), id
        assert type(c['correctAnswer']) is int and 0<=c['correctAnswer']<4,id
        assert c['explanation'].strip() and c['status'] in ('draft','approved'),id
    if file.name=='es.json': base=ids
    print(f'{file.name}: {len(cards)} cards; {dict(Counter(c["category"] for c in cards))}; {sum(c["status"]=="approved" for c in cards)} approved')
assert base is not None
for f in root.glob('*.json'):
    assert {c['id'] for c in json.loads(f.read_text())}==base, f
