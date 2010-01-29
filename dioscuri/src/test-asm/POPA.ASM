; POPA (opcode 61), push all general registers onto stack
; b8 11 11 b9 aa aa ba bb bb bb cc cc bd dd dd be ee ee bf ff ff 60
; b8 00 00 b9 00 00 ba 00 00 bb 00 00 bd 00 00 be 00 00 bf 00 00 61
start:
	MOV ax, 0x1111	; Move value into ax
	MOV cx, 0xaaaa	; Move value into cx
	MOV dx, 0xbbbb	; Move value into dx
	MOV bx, 0xcccc	; Move value into bx
	MOV bp, 0xdddd	; Move value into bp
	MOV si, 0xeeee	; Move value into si
	MOV di, 0xffff	; Move value into di
	PUSHA		; Push all registers onto stack
	MOV ax, 0x0000	; Clear register
	MOV cx, 0x0000	; Clear register
	MOV dx, 0x0000	; Clear register
	MOV bx, 0x0000	; Clear register
	MOV bp, 0x0000	; Clear register
	MOV si, 0x0000	; Clear register
	MOV di, 0x0000	; Clear register
	POPA		; Pop all registers from stack
	HLT		; Stop execution