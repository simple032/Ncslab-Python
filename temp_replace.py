with open(r'D:\NewLab\ncslab_link\src\main\java\com\ncslab\code\c\windows\simulation\CodeModelCWindowsSimulation.java', 'r', encoding='utf-8') as f:
    content = f.read()

old = '''\t\t\t\twhile(true) {
\t\t\t\t\t// Check if simulation should be stopped (session closed or thread interrupted)
\t\t\t\t\tif (session != null && !session.isOpen()) {
\t\t\t\t\t\tSystem.out.println("[CodeModelCWindowsSimulation] Session closed, stopping simulation");
\t\t\t\t\t\treturn;
\t\t\t\t\t}
\t\t\t\t\tif (Thread.interrupted()) {
\t\t\t\t\t\tSystem.out.println("[CodeModelCWindowsSimulation] Thread interrupted, stopping simulation");
\t\t\t\t\t\treturn;
\t\t\t\t\t}
\t\t\t\t\tint pre1=0,pre2=0;
\t\t\t\t\tdo {
\t\t\t\t\t\tpre1=pre2;
\t\t\t\t\t\tpre2=out.readByte();
\t\t\t\t\t\tif(pre1==0x55&&pre2==0x55) {
\t\t\t\t\t\t\tbreak;
\t\t\t\t\t\t}
\t\t\t\t\t}
\t\t\t\t\twhile(true);'''

# Let me find the exact text
idx = content.find('int pre1=0,pre2=0;')
if idx >= 0:
    # Extract surrounding context
    start = content.rfind('while(true) {', 0, idx)
    end = content.find('while(true);', idx) + len('while(true);')
    actual = content[start:end]
    print('ACTUAL:')
    print(repr(actual))
    
    new = '''\t\t\t\twhile(true) {
\t\t\t\t\t// Check if simulation should be stopped (session closed or thread interrupted)
\t\t\t\t\tif (session != null && !session.isOpen()) {
\t\t\t\t\t\tSystem.out.println("[CodeModelCWindowsSimulation] Session closed, stopping simulation");
\t\t\t\t\t\treturn;
\t\t\t\t\t}
\t\t\t\t\tif (Thread.interrupted()) {
\t\t\t\t\t\tSystem.out.println("[CodeModelCWindowsSimulation] Thread interrupted, stopping simulation");
\t\t\t\t\t\treturn;
\t\t\t\t\t}
\t\t\t\t\t// 4-byte preamble sync: 0x55 0xAA 0x55 0xAA
\t\t\t\t\tint b1 = 0, b2 = 0, b3 = 0, b4 = 0;
\t\t\t\t\tdo {
\t\t\t\t\t\tb1 = b2; b2 = b3; b3 = b4;
\t\t\t\t\t\tb4 = out.readByte() & 0xFF;
\t\t\t\t\t\tif (b1 == 0x55 && b2 == 0xAA && b3 == 0x55 && b4 == 0xAA) {
\t\t\t\t\t\t\tbreak;
\t\t\t\t\t\t}
\t\t\t\t\t} while (true);'''
    
    content = content.replace(actual, new)
    with open(r'D:\NewLab\ncslab_link\src\main\java\com\ncslab\code\c\windows\simulation\CodeModelCWindowsSimulation.java', 'w', encoding='utf-8') as f:
        f.write(content)
    print('Replaced successfully')
else:
    print('int pre1 not found')
