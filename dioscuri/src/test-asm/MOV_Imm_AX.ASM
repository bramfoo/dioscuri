; MOV_imm_AX (opcode B8), move immediate word into AX
; b8 11 b0 ff
start:
	MOV AX, 0x1234
	MOV AX, 0xABCD
	HLT		; Stop execution