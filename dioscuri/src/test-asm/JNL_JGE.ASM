; JNL / JGE (opcode 7D), jump if sign == overflow
; b8 01 80 48 7d fa 7d f8 48 7d f5 40 7d 01 90 48 48 7d ed
start:
	MOV ax, 0x8001	; Set ax to 8001
	DEC ax		; Decrement ax to set sign, clear overflow
	JNL start	; Jump to beginning (fail, s=1 o=0)
	JGE start	; Show ASM instruction has identical bytecode
	DEC ax		; Decrement ax to clear sign, set overflow
	JNL start	; Jump to beginning (fail, s=0 o=1)
	INC ax		; Increment ax to set sign, overflow
	JNL setsgnover	; Jump to clearsignsetoverflow (pass, s=o=1)
	NOP		; Filler to demonstrate jump
setsgnover:
	DEC ax		; Decrement ax
	DEC ax		; Decrement ax to clear sign, overflow
	JNL start	; Jump to beginning (pass, s=o=0)
