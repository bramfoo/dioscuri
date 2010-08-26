; SAHF (opcode 9E), load the FLAGS register with values from AH register
; b4 d7 9e b4 02 9e f4
start:
	MOV AH, 0xD7	; Move 11010111b into AH
	SAHF		; Load AH into FLAGS
	MOV AH, 0x02	; Move 00000010b into AH
	SAHF		; Load AH into FLAGS
	HLT		; Stop execution
