; STD (opcode FD), set direction flag (set to 1)
; fd fd f4
start:
	STD		; Clear direction flag
	STD		; Set direction flag
	HLT		; Stop execution