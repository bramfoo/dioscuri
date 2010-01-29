; CWD (opcode 99), Convert Word to DoubleWord, extending AX sign to DX
; b4 80 99 b4 7f 99 f4
start:
	MOV AH, 0x80	; Move 0x80 into AH
	CWD		; Change DX to 0xFFFF
	MOV AH, 0x7F	; Move 0x7F into AH
	CWD		; Change DX to 0x0000
	HLT		; Stop execution