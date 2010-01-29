; SBB_AL_Ib (opcode 1C), subtract (immediate byte + CF) from AL
; f5 b8 7f 80 1c ff 40 1c 00 1c 7f 1c 01 1c 00 f4
start:
	CMC		; Set carry flag
	MOV ax, 0x807F	; Move value into AX
	SBB al, 0xFF	; Test AF flag
	INC ax		; Set flags
	SBB al, 0x00	; Test OF, AF, SF, PF flags
	SBB al, 0x7f	; Test ZF
	SBB al, 0x01	; Test CF, SBB, overflow
	SBB al, 0x00	; Test CF, PF
	HLT