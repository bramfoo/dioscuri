; CLD (opcode FC), clear direction flag (set to 0)
; fc fd fc f4
start:
	CLD		; Clear direction flag
	STD		; Set direction flag
	CLD		; Clear direction flag
	HLT		; Stop execution