import os

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# root = lunarix client dir (mappings/ is inside it)
jars = [
    os.path.join(root, '.fabric', 'remappedJars', 'minecraft-1.21.11-0.20.1', 'client-intermediary.jar'),
    os.path.join(root, 'mods', 'fabric-api-0.141.6-1.21.11.jar'),
    os.path.expanduser(r'~\.gradle\caches\modules-2\files-2.1\org.projectlombok\lombok\1.18.38\57f8f5e02e92a30fd21b80cbd426a4172b5f8e29\lombok-1.18.38.jar'),
]
lib = os.path.join(os.environ['APPDATA'], '.minecraft', 'libraries')
for dp, _, fs in os.walk(lib):
    for f in fs:
        if f.endswith('.jar') and 'sources' not in f and 'javadoc' not in f:
            jars.append(os.path.join(dp, f))

# nested jars inside fabric-api mod jar
import zipfile, shutil
nested_dir = os.path.join(root, 'temp_nested_jars')
os.makedirs(nested_dir, exist_ok=True)
for jar_path in [os.path.join(root, 'mods', 'fabric-api-0.141.6-1.21.11.jar')]:
    with zipfile.ZipFile(jar_path) as z:
        for n in z.namelist():
            if n.startswith('META-INF/jars/') and n.endswith('.jar'):
                out = os.path.join(nested_dir, os.path.basename(n))
                if not os.path.exists(out):
                    with z.open(n) as src, open(out, 'wb') as dst:
                        shutil.copyfileobj(src, dst)
                jars.append(out)

# embedded dependency jars shipped inside the client sources
emb = os.path.join(root, 'src', 'main', 'java', 'META-INF', 'jars')
for f in os.listdir(emb):
    if f.endswith('.jar'):
        jars.append(os.path.join(emb, f))

cp = ';'.join(jars)
files = []
for dp, _, fs in os.walk(os.path.join(root, 'src', 'main', 'java')):
    for f in fs:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

def q(p):
    return '"' + p.replace('\\', '/') + '"'

out_dir = os.path.join(root, 'temp_atmo_classes')
with open(os.path.join(root, 'javac_args.txt'), 'w', encoding='utf-8') as out:
    out.write('-encoding\nUTF-8\n-nowarn\n-proc:none\n-d\n%s\n-cp\n%s\n' % (q(out_dir), q(cp.replace('\\', '/'))))
    for f in files:
        out.write(q(f) + '\n')
print(len(jars), 'jars', len(files), 'files')
