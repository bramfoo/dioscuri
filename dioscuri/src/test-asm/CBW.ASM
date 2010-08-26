; CBW (opcode 98), Convert Byte to Word, extending AL sign to every bit of AH
; b0 80 98 b0 7f 98 f4
start:
	MOV AL, 0x80	; Move 0x80 into AL
	CBW		; Change AH to 0xFF
	MOV AL, 0x7F	; Move 0x7F into AL
	CBW		; Change AH to 0x00
	HLT		; Stop execution