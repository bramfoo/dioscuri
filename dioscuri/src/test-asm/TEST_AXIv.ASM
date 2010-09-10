; TEST_AXIv (opcode A9), logical compare (AND) of immediate word and AX
; 48 a9 55 55 a9 a8 aa a9 00 00 f4
start:
	DEC ax
	TEST ax, 0x5555   ; 1111 1111 1111 1111 | 0101 0101 0101 0101 (SF, PF)
	TEST ax, 0xAAA8   ; 0101 0101 0101 0101 | 1010 1010 1010 1010 (SF, PF)
	TEST ax, 0x0000   ; 1111 1111 1111 1111 | 0000 0000 0000 0000 (SF, ZF, PF)
	HLT