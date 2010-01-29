; CLC (opcode F8), clear carry flag (set to 0)
; f8 2c 01 f8 f4
start:
	CLC		; Clear carry flag
	SUB AL, 0x01	; Move 0xFF into AX
	CLC		; Clear carry flag
	HLT		; Stop execution