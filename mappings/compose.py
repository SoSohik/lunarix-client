import re, sys

MOJMAP = r"C:\Users\Адмін\OneDrive\Desktop\lunarix client\mappings\mojang_1.21.11.txt"
TINY = r"C:\Users\Адмін\.gradle\caches\fabric-loom\1.21.11\net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2\mappings.tiny"
OUT = r"C:\Users\Адмін\OneDrive\Desktop\lunarix client\mappings\mojmap_to_intermediary.txt"

PRIMS = {'void':'V','boolean':'Z','byte':'B','char':'C','short':'S','int':'I','long':'J','float':'F','double':'D'}

def type_to_desc(t):
    t = t.strip()
    if not t:
        return ''
    dims = 0
    while t.endswith('[]'):
        dims += 1
        t = t[:-2].strip()
    if t.endswith('...'):
        dims += 1
        t = t[:-3].strip()
    if t in PRIMS:
        d = PRIMS[t]
    else:
        d = 'L' + t.replace('.', '/') + ';'
    return '[' * dims + d

def split_args(s):
    parts, depth, cur = [], 0, ''
    for ch in s:
        if ch == '<': depth += 1
        elif ch == '>': depth -= 1
        if ch == ',' and depth == 0:
            parts.append(cur.strip()); cur = ''
        else:
            cur += ch
    if cur.strip():
        parts.append(cur.strip())
    return parts

# ---------- pass 1: class headers ----------
cls_moj2obf = {}
raw_members = {}
cur = None
with open(MOJMAP, encoding='utf-8', errors='replace') as f:
    for line in f:
        if line.startswith('#') or not line.strip():
            continue
        if not line.startswith((' ', '\t')):
            m = re.match(r'^(\S+)\s+->\s+(\S+):\s*$', line)
            if m:
                cur = m.group(1)
                cls_moj2obf[cur] = m.group(2)
                raw_members.setdefault(cur, [])
            continue
        if cur is not None:
            raw_members[cur].append(line)

def desc_to_obf(desc):
    def repl(m):
        inner = m.group(1)
        key = inner.replace('/', '.')
        if key in cls_moj2obf:
            return 'L' + cls_moj2obf[key] + ';'
        return m.group(0)
    return re.sub(r'L([\w/$]+);', repl, desc)

# ---------- pass 2: members ----------
members = {}
for mc, lines in raw_members.items():
    for line in lines:
        s = line.strip()
        if '->' not in s:
            continue
        left, obf = s.rsplit('->', 1)
        left = re.sub(r'^\d+:\d+:', '', left.strip())
        obf = obf.strip().rstrip(':')
        if '(' in left:
            mm = re.match(r'^(.*?)\s+(\S+)\((.*)\)\s*(.*)$', left)
            if not mm:
                continue
            ret, name, args, tail = mm.groups()
            ret_t = (tail.strip() or ret.strip())
            args_clean = re.sub(r'<[^<>]*>', '', args)
            ret_clean = re.sub(r'<[^<>]*>', '', ret_t)
            desc = '(' + ''.join(type_to_desc(a) for a in split_args(args_clean) if a) + ')' + type_to_desc(ret_clean)
            members.setdefault(mc, []).append(('m', desc_to_obf(desc), obf, name, left))
        else:
            parts = left.rsplit(' ', 1)
            if len(parts) == 2:
                typ, name = parts
                typ_clean = re.sub(r'<[^<>]*>', '', typ.strip())
                d = desc_to_obf(type_to_desc(typ_clean))
                members.setdefault(mc, []).append(('f', d, obf, name, left))

# ---------- parse yarn tiny ----------
obf2int_cls = {}
mem = {}
mem_f = {}
fld = {}
cur_cls = None
with open(TINY, encoding='utf-8', errors='replace') as f:
    for line in f:
        line = line.rstrip('\n')
        if not line or line.startswith('#'):
            continue
        parts = line.split('\t')
        if parts[0] == '' and len(parts) > 1:
            parts = parts[1:]
        if parts[0] == 'c' and len(parts) >= 3:
            cur_cls = parts[2]
            obf2int_cls[parts[1]] = parts[2]
        elif parts[0] in ('m', 'f') and len(parts) >= 5:
            kind, desc, off, itn = parts[0], parts[1], parts[2], parts[3]
            if cur_cls is not None:
                if kind == 'm':
                    mem[(cur_cls, desc, off)] = itn
                else:
                    mem_f[(cur_cls, desc, off)] = itn
                    fld[(cur_cls, desc, off)] = itn

out = open(OUT, 'w', encoding='utf-8')
needed = [l.strip() for l in sys.argv[1:]] if len(sys.argv) > 1 else list(cls_moj2obf.keys())
missing = 0
for mc in needed:
    if mc not in cls_moj2obf:
        out.write(f"# NOT FOUND class {mc}\n")
        continue
    obf = cls_moj2obf[mc]
    icls = obf2int_cls.get(obf, obf)
    out.write(f"# {mc} -> {icls}\n")
    for kind, desc, obf_name, moj_name, raw in members.get(mc, []):
        table = mem if kind == 'm' else fld
        itn = table.get((icls, desc, obf_name))
        if itn is None:
            cands = [n for (c, d, o), n in table.items() if c == icls and o == obf_name]
            itn = cands[0] if len(cands) == 1 else ('AMBIG:' + ','.join(cands) if cands else '?')
        if itn is None or str(itn) == '?' or str(itn).startswith('AMBIG'):
            missing += 1
        out.write(f"{moj_name}\t{raw}\t->\t{itn}\n")
out.close()
print("done, unresolved:", missing)
