; DEC_BX (opcode 4B), decrement BX
; 4b bb 00 80 4b bb 01 00 4b f4
start:
	DEC bx		; Decrement bx, test AF
	MOV bx, 0x8000	; Prepare for OF
	DEC bx		; Decrement bx, test OF, SF
	MOV bx, 0x0001	; Prepare for ZF
	DEC bx		; Decrement bx, test ZF
	HLT