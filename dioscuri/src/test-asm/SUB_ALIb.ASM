; SUB_AL_Ib (opcode 2C), subtract immediate byte from AL
; b8 85 01 2c 05 2c 05 2c 7b 2c 02 2c 02 f4
start:
	MOV ax, 0x0185	; Move value into AX
	SUB al, 0x05	; Test SF, PF flags
	SUB al, 0x05	; Test OF, AF, SF, PF flags
	SUB al, 0x7b	; Test ZF
	SUB al, 0x02	; Test CF, overflow
	SUB al, 0x02	; Test CF, PF
	HLT