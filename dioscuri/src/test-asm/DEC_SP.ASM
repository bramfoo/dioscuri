; DEC_sp (opcode 4c), decrement sp
; 4c bc 00 80 4c bc 01 00 4c f4
start:
	MOV sp, 0x0000	; Initialise
	DEC sp		; Decrement sp, test AF
	MOV sp, 0x8000	; Prepare for OF
	DEC sp		; Decrement sp, test OF, SF
	MOV sp, 0x0001	; Prepare for ZF
	DEC sp		; Decrement sp, test ZF
	HLT