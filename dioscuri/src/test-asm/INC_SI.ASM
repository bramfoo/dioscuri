; INC_si (opcode 46), increment si
; be 0f 46 be ff 7f 46 be ff ff 46 46 f4
start:
	MOV si, 0x0F	; Prepare for AF
	INC si		; Increment si, test AF
	MOV si, 0x7FFF	; Prepare for OF
	INC si		; Increment si, test OF, SF
	MOV si, 0xFFFF	; Prepare for ZF
	INC si		; Increment si, test ZF
	INC si		; Increment si, test !AF
	HLT