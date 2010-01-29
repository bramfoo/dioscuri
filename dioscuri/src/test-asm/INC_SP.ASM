; INC_sp (opcode 44), increment sp
; bc 0f 44 bc ff 7f 44 bc ff ff 44 44 f4
start:
	MOV sp, 0x0F	; Prepare for AF
	INC sp		; Increment sp, test AF
	MOV sp, 0x7FFF	; Prepare for OF
	INC sp		; Increment sp, test OF, SF
	MOV sp, 0xFFFF	; Prepare for ZF
	INC sp		; Increment sp, test ZF
	INC sp		; Increment sp, test !AF
	HLT