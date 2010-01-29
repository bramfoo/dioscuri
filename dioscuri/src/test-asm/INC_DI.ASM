; INC_di (opcode 47), increment di
; bf 0f 47 bf ff 7f 47 bf ff ff 47 47 f4
start:
	MOV di, 0x0F	; Prepare for AF
	INC di		; Increment di, test AF
	MOV di, 0x7FFF	; Prepare for OF
	INC di		; Increment di, test OF, SF
	MOV di, 0xFFFF	; Prepare for ZF
	INC di		; Increment di, test ZF
	INC di		; Increment di, test !AF
	HLT