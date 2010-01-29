; JL / JNGE (opcode 7C), jump if sign != overflow
; b8 fe 7f 40 7c fa 7c f8 40 7c f5 40 7c 1 90 b8 00 80 48 7c eb
start:
	MOV ax, 0x7FFE	; Set ax to 7FFE
	INC ax		; Increment ax to clear sign, overflow
	JL  start	; Jump to beginning (fail, s=o=0)
	JNGE start	; Show ASM instruction has identical bytecode
	INC ax		; Increment ax to set sign, overflow
	JL start	; Jump to beginning (fail, s=o=1)
	INC ax		; Increment ax to clear overflow
	JL clsgnseto	; Jump to clearsignsetoverflow (pass, s=1, o=0)
	NOP		; Filler to demonstrate jump
clsgnseto:
	MOV ax, 0x8000	; Set ax to 8000
	DEC ax		; Decrement ax to clear sign, set overflow
	JL  start	; Jump to beginning (pass, s=0 o=1)


