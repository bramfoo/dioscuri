; INC_cx (opcode 41), increment cx
; b1 0f 41 b9 ff 7f 41 b9 ff ff 41 41 f4
start:
	MOV cl, 0x0F	; Prepare for AF
	INC cx		; Increment cx, test AF
	MOV cx, 0x7FFF	; Prepare for OF
	INC cx		; Increment cx, test OF, SF
	MOV cx, 0xFFFF	; Prepare for ZF
	INC cx		; Increment cx, test ZF
	INC cx		; Increment cx, test !AF
	HLT