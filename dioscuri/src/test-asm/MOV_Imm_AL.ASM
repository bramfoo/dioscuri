; MOV_AL (opcode B0), move immediate byte into AL
; b0 11 b0 ff
start:
	MOV AL, 0x11
	MOV AL, 0xFF
	HLT		; Stop execution