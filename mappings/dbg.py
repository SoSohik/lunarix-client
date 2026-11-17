import re
src = open('compose.py', encoding='utf-8').read()
exec(src)
print('icls for gfj:', obf2int_cls.get('gfj'))
print('mem hit:', mem.get(('net/minecraft/class_310', '()Lgfj;', 'V')))
print('fld sample:', [k for k in list(fld)[:3]])
# count mem entries under class_310
n = sum(1 for k in mem if k[0]=='net/minecraft/class_310')
print('class_310 members:', n)
