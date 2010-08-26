; JNLE / JG (opcode 7F), jump if not zero and sign == overflow
; b8 ff ff 40 7f fa 7f f8 b8 01 80 48 7f f2 48 7f ef 40 7f 01 90 48 48 7f e7
start:
	MOV ax, 0xFFFF	; Set ax to FFFF
	INC ax		; Increment ax to clear sign, overflow, set zero
	JNLE start	; Jump to beginning (fail, s=o=0 z=1)
	JG start	; Show ASM instruction has identical bytecode
	MOV ax, 0x8001	; Set ax to 8001
	DEC ax		; Decrement ax to set sign, clear overflow
	JNLE start	; Jump to beginning (fail, s=1 o=z=0)
	DEC ax		; Decrement ax to clear sign, set overflow
	JNLE start	; Jump to beginning (fail, s=0 o=1 z=0)
	INC ax		; Increment ax to set sign, overflow
	JNLE clsoz	; Jump to clearsignoverflowzero (pass, s=o=1 z=0)
	NOP		; Filler to demonstrate jump
clsoz:
	DEC ax		; Decrement ax
	DEC ax		; Decrement ax to clear sign, overflow
	JNLE start	; Jump to beginning (pass, s=o=0)