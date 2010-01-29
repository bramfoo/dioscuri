; Simple assembler program to loop through various instructions
; Can be used as a speed test for the CPU instructions
; b8 ff ff a3 00 00 50 89 c1 85 46 00 23 4e 00 51 89 c2 3b 46 00 2b 56 00
; 52 09 c8 31 d0 5a 59 58 48 75 e3 43 75 e0 f4
setup:
	MOV AX, 0xFFFF
	MOV [0000], AX		; Store 0xFFFF value in memory
	MOV BX, 0xFF00
start:
	PUSH AX

	MOV CX, AX		; Apply instructions
	TEST AX,[BP+0x0000]
	AND CX, [BP+0x0000]
	PUSH CX

	MOV DX, AX		; Apply more instructions
	CMP AX, [BP+0x0000]
	SUB DX, [BP+0x0000]
	PUSH DX

	OR AX, CX		; Apply few more instructions
	XOR AX, DX

	POP DX			; Return original values
	POP CX
	POP AX

	DEC AX			; Decrement AX for loop
	JNZ start		; Jump to start while AX != 0

	INC BX			; Increment BX for loop
	JNZ start		; Jump to start while BX != 0

	HLT			; Stop execution