; DEC_bp (opcode 4d), decrement bp
; 4d bd 00 80 4d bd 01 00 4d f4
start:
	DEC bp		; Decrement bp, test AF
	MOV bp, 0x8000	; Prepare for OF
	DEC bp		; Decrement bp, test OF, SF
	MOV bp, 0x0001	; Prepare for ZF
	DEC bp		; Decrement bp, test ZF
	HLT