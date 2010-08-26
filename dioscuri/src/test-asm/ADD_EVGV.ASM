; ADD_EvGv (opcode 01), Add two words
; b8 01 00 bb 01 00 01 c3 b8 ff 7f bb 01 00 01 c3 b8 00 80 bb 01 80 01 c3 f4
start:
	; Check 1 + 1 = 2
	MOV AX, 0x0001	  ; Move 0x0001 into AX
	MOV BX, 0x0001	  ; Move 0x0001 into BX
	ADD AX, BX	  ; Add BX to AX

	; Check overflow (signed) -> should not happen
	MOV AX, 0x7F	; Move 0x7F into AX
	MOV BX, 0x01	; Move 0x01 into BX
	ADD AX, BX	  ; Add BX to AX

	; Check overflow (signed) -> should happen
	MOV AX, 0x7FFF	  ; Move 0x7FFF into AX
	MOV BX, 0x0001	  ; Move 0x0001 into BX
	ADD AX, BX	  ; Add BX to AX

	; Check underflow (signed) and carry (unsigned) -> should happen
	MOV AX, 0x8000	  ; Move 0x8000 into AX
	MOV BX, 0x8001	  ; Move 0x8001 into BX
	ADD AX, BX	  ; Add BX to AX

	HLT		  ; Stop execution
