; TEST_ALIb (opcode A8), logical compare (AND) of immediate byte and AL
; 48 A8 54 A8 aa A8 00 f4
start:
	DEC ax		; Set AL to FF
	TEST al, 0x54	; 1111 1111 | 0101 0101 (SF, PF)
	TEST al, 0xAA	; 1010 1010 | 1010 1010 (SF, PF)
	TEST al, 0x00	; 1111 1111 | 0000 0000 (SF, ZF, PF)
	HLT