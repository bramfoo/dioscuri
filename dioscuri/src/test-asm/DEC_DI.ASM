; DEC_di (opcode 4f), decrement di
; 4f bf 00 80 4f bf 01 00 4f f4
start:
	DEC di		; Decrement di, test AF
	MOV di, 0x8000	; Prepare for OF
	DEC di		; Decrement di, test OF, SF
	MOV di, 0x0001	; Prepare for ZF
	DEC di		; Decrement di, test ZF
	HLT