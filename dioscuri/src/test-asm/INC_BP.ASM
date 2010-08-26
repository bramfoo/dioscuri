; INC_bp (opcode 45), increment bp
; bd 0f 45 bd ff 7f 45 bd ff ff 45 45 f4
start:
	MOV bp, 0x0F	; Prepare for AF
	INC bp		; Increment bp, test AF
	MOV bp, 0x7FFF	; Prepare for OF
	INC bp		; Increment bp, test OF, SF
	MOV bp, 0xFFFF	; Prepare for ZF
	INC bp		; Increment bp, test ZF
	INC bp		; Increment bp, test !AF
	HLT