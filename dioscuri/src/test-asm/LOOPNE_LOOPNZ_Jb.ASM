; LOOPNE / LOOPNZ (opcode E0), loop while CX != 0 and ZF == 0
; B1 05 B0 01 90 E0 FD B1 05 48 E0 F8 F4
start:
	MOV CL, 0x05	; Set loop counter to 5
	MOV AL, 0x01	; Set AL to 2
startloop:
	NOP		; No operation
	LOOPNE startloop; Return to start of loop, pass until CX == 0
	MOV CL, 0x05	; Set loop counter to 5
	DEC AX		; Decrement AX, setting zero flag
	LOOPNZ startloop; Return to start of loop, fail because ZF == 1
	HLT		; End