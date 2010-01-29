; DEC_DX (opcode 4A), decrement DX
; 4a ba 00 80 4a ba 01 00 4a f4
start:
	DEC dx		; Decrement dx, test AF
	MOV dx, 0x8000	; Prepare for OF
	DEC dx		; Decrement dx, test OF, SF
	MOV dx, 0x0001	; Prepare for ZF
	DEC dx		; Decrement dx, test ZF
	HLT