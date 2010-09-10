; LOOPE / LOOPZ (opcode E1), loop while CX != 0 and ZF==1
; b1 05 b0 01 90 e1 fd 48 e1 fa f4
start:
	MOV CL, 0x05	; Set loop counter to 5
	MOV AL, 0x01	; Set AL to 3
startloop:
	NOP		; No operation
	LOOPE startloop ; Return to start of loop, fail first time
	DEC AX		; Decrement AX, setting zero flag
	LOOPZ startloop ; Return to start of loop, pass
	HLT		; End