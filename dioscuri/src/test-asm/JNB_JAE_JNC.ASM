; JNB / JAE / JNC (opcode 73), jump not carry
; 73 7 73 5 73 3 f8 eb f7 f5 eb f4
start:
	JNB setcarry	; Jump to setcarry if no carry
	JAE setcarry	; Show ASM instruction has identical bytecode
	JNC setcarry	; Show ASM instruction has identical bytecode
	CLC		; Toggle carry flag
	JMP start	; Return to beginning
setcarry:
	CMC
	JMP start	; Jump to beginning
