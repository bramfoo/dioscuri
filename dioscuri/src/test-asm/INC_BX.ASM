; INC_bx (opcode 43), increment bx
; b3 0f 43 bb ff 7f 43 bb ff ff 43 43 f4
start:
	MOV bl, 0x0F	; Prepare for AF
	INC bx		; Increment bx, test AF
	MOV bx, 0x7FFF	; Prepare for OF
	INC bx		; Increment bx, test OF, SF
	MOV bx, 0xFFFF	; Prepare for ZF
	INC bx		; Increment bx, test ZF
	INC bx		; Increment bx, test !AF
	HLT