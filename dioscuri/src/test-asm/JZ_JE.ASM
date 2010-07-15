; JZ / JE (opcode 74), jump on zero
; 74 8 74 6 b8 ff ff 40 eb f6 40 eb f3
start:
	JZ clearzero	; Jump to clearzero if zero set
	JE clearzero	; Show ASM instruction has identical bytecode
	MOV ax, 0xFFFF	; Set ax to FFFF
	INC ax		; Increment ax to set zero flag
	JMP start	; Return to beginning
clearzero:
	INC ax		; Increment ax to clear zero flag
	JMP start	; Jump to beginning
