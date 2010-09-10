; JNZ / JNE (opcode 75), jump not zero
; 75 5 75 3 40 eb f9 b8 ff ff 40 eb f3
start:
	JNZ setzero	; Jump to setzero if zero clear
	JNE setzero	; Show ASM instruction has identical bytecode
	INC ax		; Increment ax to clear zero flag
	JMP start	; Return to beginning
setzero:
	MOV ax, 0xFFFF	; Set ax to FFFF
	INC ax		; Increment ax to set zero flag
	JMP start	; Jump to beginning
