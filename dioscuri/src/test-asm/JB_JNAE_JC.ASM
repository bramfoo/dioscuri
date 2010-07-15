; JB / JNAE / JC (opcode 72), jump on carry
; 72 7 72 5 72 3 f5 72 f5 f8 eb f4
start:
	JB clearcarry	; Jump to clearcarry if no carry
	JNAE clearcarry ; Show ASM instruction has identical bytecode
	JC clearcarry	; Show ASM instruction has identical bytecode
	CMC		; Toggle carry flag
	JB -2		; Jump to IP 98 (start at 100), to test full IP range
clearcarry:
	CLC
	JMP start	; Jump to beginning

