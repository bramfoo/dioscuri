; STI (opcode FB), set interrupt flag (set to 1)
; fb fa fb f4
start:
	STI		; Clear interrupt flag
	CLI		; Clear interrupt flag
	STI		; Clear interrupt flag
	HLT		; Stop execution