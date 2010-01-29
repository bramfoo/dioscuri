; JNS (opcode 79), jump not sign
; b8 ff 7f 40 79 fa b8 fe 7f 40 79 f4
start:
	MOV ax,0x7FFF	; Store 7FFF in ax
	INC ax		; Increment ax to set signed flag
	JNS start	; Jump if not signed (fail)
	MOV ax,0x7FFE	; Store 7FFE in ax
	INC ax		; Increment ax to clear signed flag
	JS start	; Jump if not signed (pass)