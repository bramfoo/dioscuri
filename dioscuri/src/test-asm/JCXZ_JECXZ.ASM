; JCXZ (opcode E3), jump if CX is zero
; 41 e3 fd 49 e3 fa
start:
	INC cx		; Increment cx
	JCXZ start	; Jump to beginning (fail, cx != 0)
	DEC cx		; Decrement cx to zero
	JCXZ start	; Jump to beginning (pass, cx == 0)