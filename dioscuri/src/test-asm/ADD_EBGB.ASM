; ADD_EbGb (opcode 00), Add two bytes
; b0 01 b3 01 00 c3 b0 7f b3 01 00 c3 b0 80 b3 81 00 c3 f4
start:
	; Check 1 + 1 = 2
	MOV AL, 0x01	  ; Move 0x01 into AL
	MOV BL, 0x01	  ; Move 0x01 into BL
	ADD AL, BL	  ; Add BL to AL

	; Check overflow (signed) -> should happen
	MOV AL, 0x7F	  ; Move 0x7F into AL
	MOV BL, 0x01	  ; Move 0x01 into BL
	ADD AL, BL	  ; Add BL to AL

	; Check underflow (signed) and carry (unsigned) -> should happen
	MOV AL, 0x80	  ; Move 0x80 into AL
	MOV BL, 0x81	  ; Move 0x81 into BL
	ADD AL, BL	  ; Add BL to AL

	HLT		  ; Stop execution
