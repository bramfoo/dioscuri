; CMP_AXIv (opcode 3D), compare immediate word with AX
; 3d 00 80 b8 01 81 3d ff 00 3d 02 01 3d 01 81 3d 02 81 b8 01 7f 3d 01 ff f4
start:
	CMP ax, 0x8000	; Test OF, SF, CF
	MOV ax, 0x8101	; Move value into AX
	CMP ax, 0x00FF	; Test SF, PF flags
	CMP ax, 0x0102	; Test OF, SF, AF, PF flags
	CMP ax, 0x8101	; Test ZF
	CMP ax, 0x8102	; Test AF, CF, overflow
	MOV ax, 0x7F01	; Move value into AX
	CMP ax, 0xFF01	; Test OF, CF
	HLT