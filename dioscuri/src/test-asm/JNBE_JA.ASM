; JNBE / JA (opcode 77), jump not carry and not zero
; b8 ff ff 40 f5 77 f9 f5 77 f6 f5 40 77 f2 f5 77 ef 77 ed
start:
	MOV ax, 0xFFFF	; Set ax to FFFF
	INC ax		; Increment ax to set zero flag
	CMC		; Set carry flag
	JNBE start	; Return to beginning (fail, C=Z=1)
	CMC		; Clear carry flag
	JNBE start	; Return to beginning (fail, C=0 Z=1)
	CMC		; Set carry flag
	INC ax		; Increment ax to clear zero flag
	JNBE start	; Return to beginning (fail, C=1 Z=0)
	CMC		; Clear carry flag
	JNBE start	; Return to beginning (pass, C=0 Z=0)
	JA start	; Show ASM instruction has identical bytecode
