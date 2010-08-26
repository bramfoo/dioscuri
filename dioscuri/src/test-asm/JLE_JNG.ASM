; JLE / JNG (opcode 7E), jump if zero or sign != overflow
; b8 fe 7f 40 7e fa 7e f8 40 7e f5 40 7e 1 90 b8 00 80 48 7e 01 90 b8 ff ff 40 7e e4
start:
	MOV ax, 0x7FFE	; Set ax to 7FFE
	INC ax		; Increment ax to clear sign, overflow
	JLE start	; Jump to beginning (fail, s=o=z=0)
	JNG start	; Show ASM instruction has identical bytecode
	INC ax		; Increment ax to set sign, overflow
	JLE start	; Jump to beginning (fail, s=o=1, z=0)
	INC ax		; Increment ax to clear overflow
	JLE clsgnseto	; Jump to clearsignsetoverflow (pass, s=1, o=z=0)
	NOP		; Filler to demonstrate jump
clsgnseto:
	MOV ax, 0x8000	; Set ax to 8000
	DEC ax		; Decrement ax to clear sign, set overflow
	JLE  zero	; Jump to beginning (pass, s=0 o=1, z=0)
	NOP		; Filler to demonstrate jump
zero:
	MOV ax, 0xFFFF	; Set ax to 8000
	INC ax		; Decrement ax to clear sign, set overflow
	JLE start	; Jump to beginning (pass, s=o=0, z=1)



