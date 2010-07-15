; LOOP (opcode E0), loop while CX != 0
; b1 03 40 e2 fd f4
start:
	MOV CL, 0x03	; Set loop counter to 3
startloop:
	INC ax		; Show loop running
	LOOP startloop	; Return to start of loop, fail first time
	HLT		; End