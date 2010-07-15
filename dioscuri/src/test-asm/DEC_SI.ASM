; DEC_si (opcode 4e), decrement si
; 4e be 00 80 4e be 01 00 4e f4
start:
	DEC si		; Decrement si, test AF
	MOV si, 0x8000	; Prepare for OF
	DEC si		; Decrement si, test OF, SF
	MOV si, 0x0001	; Prepare for ZF
	DEC si		; Decrement si, test ZF
	HLT