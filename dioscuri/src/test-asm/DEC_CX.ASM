; DEC_CX (opcode 49), decrement CX
; 49 b9 00 80 49 b9 01 00 49 f4
start:
	DEC cx		; Decrement cx, test AF
	MOV cx, 0x8000	; Prepare for OF
	DEC cx		; Decrement cx, test OF, SF
	MOV cx, 0x0001	; Prepare for ZF
	DEC cx		; Decrement cx, test ZF
	HLT