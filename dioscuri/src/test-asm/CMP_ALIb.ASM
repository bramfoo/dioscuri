; CMP_ALIb (opcode 0C), compare immediate byte with AL
; 3c 80 b8 85 01 3c 06 3c 85 3c 86 b8 7f 01 3c ff f4
start:
	CMP al, 0x0080	; Test OF, SF, CF
	MOV ax, 0x0185	; Move value into ax
	CMP al, 0x06	; Test OF, SF, AF, PF flags
	CMP al, 0x85	; Test ZF
	CMP al, 0x86	; Test AF, CF, overflow
	MOV ax, 0x017F	; Move value into al
	CMP al, 0xFF	; Test OF, CF
	HLT