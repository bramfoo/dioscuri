; JS (opcode 78), jump on sign
; 78 fe b8 ff 7f 40 78 f8
start:
	JS start	; Jump if signed (fail)
	MOV ax,0x7FFF	; Store 7FFF in ax
	INC ax		; Increment ax to set signed flag
	JS start	; Jump if signed (pass)