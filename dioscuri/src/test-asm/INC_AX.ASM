; INC_AX (opcode 40), increment AX
; b0 0f 40 b8 ff 7f 40 b8 ff ff 40 40 f4
start:
	MOV al, 0x0F	; Prepare for AF
	INC ax		; Increment AX, test AF
	MOV ax, 0x7FFF	; Prepare for OF
	INC ax		; Increment AX, test OF, SF
	MOV ax, 0xFFFF	; Prepare for ZF
	INC ax		; Increment AX, test ZF
	INC ax		; Increment AX, test !AF
	HLT