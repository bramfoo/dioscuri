; PUSHF (opcode 9C), push FLAGS register onto stack
; 9c b8 ff 7f 40 9d eb f8
start:
	PUSHF		; Push FLAGS onto stack
	MOV ax, 0x7fff	; Move value into ax
	INC ax		; Increment ax to change flags
	POPF		; Pop FLAGS from stack
	JMP start      ; Return to beginning