; JBE / JNA (opcode 76), jump carry or zero
; 40 B8 FF FF 76 FA F5 76 FD 40 76 FA 76 FA
start:
	INC ax		; Clear zero flag
	MOV ax, 0xFFFF	; Set ax to FFFF
	JBE start	; Return to beginning (will fail as C=Z=0)
setcarry:
	CMC		; Toggle carry
	JBE setcarry	; Jump to carry toggle
	INC ax		; Increment ax to toggle zero flag
	JBE setcarry	; Jump to carry toggle
	JNA setcarry	; Show ASM instruction has identical bytecode
