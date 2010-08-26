; DEC_AX (opcode 48), decrement AX
; 48 b8 00 80 48 b8 01 00 48 f4
start:
	DEC ax		; Decrement AX, test AF
	MOV ax, 0x8000	; Prepare for OF
	DEC ax		; Decrement AX, test OF, SF
	MOV ax, 0x0001	; Prepare for ZF
	DEC ax		; Decrement AX, test ZF
	HLT