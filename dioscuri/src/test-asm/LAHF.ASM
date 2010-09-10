; LAHF (opcode 9F), load AH register with values from the FLAGS register
; 9f bb ff 7f 43 9f f4
start:
	LAHF		; Load FLAGS into AH
	MOV BX, 0x7FFF	; Move 0x7FFF into BX
	INC BX		; Increment BX, setting flags
	LAHF		; Load FLAGS into AH
	HLT		; Stop execution