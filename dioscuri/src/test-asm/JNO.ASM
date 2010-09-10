; JNO (opcode 71), jump not overflow
; 71 3 40 eb fb b8 ff 7f 40 eb f5
start:
	JNO causeoflow	; Jump to causeoflow if no overflow
	INC ax		; Increase AX to set no overflow
	JMP start	; Return to beginning
causeoflow:
	MOV ax, 0x7FFF	; Set AX to 7FFF
	INC ax		; Increase AX to cause overflow
	JMP start	; Jump to beginning

