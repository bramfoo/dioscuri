; INC_dx (opcode 42), increment dx
; b2 0f 42 ba ff 7f 42 ba ff ff 42 42 f4
start:
	MOV dl, 0x0F	; Prepare for AF
	INC dx		; Increment dx, test AF
	MOV dx, 0x7FFF	; Prepare for OF
	INC dx		; Increment dx, test OF, SF
	MOV dx, 0xFFFF	; Prepare for ZF
	INC dx		; Increment dx, test ZF
	INC dx		; Increment dx, test !AF
	HLT