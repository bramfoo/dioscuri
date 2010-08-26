; XOR_GvEv (opcode 33), Logical word-sized XOR of register (destination) and memory/register (source)
; b8 ff ff a3 00 00 a3 02 00 b8 55 aa ba 55 aa 33 00 33 51 02
; 45 33 96 00 01 31 c0 f4
start:
	; Load memory with pre-arranged values FFFF FFFF
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0xAA55	; Move AA55 into AX
	MOV DX, 0xAA55	; Move AA55 into DX

	; XOR reg,mem
	XOR AX, [BX+SI] ; XOR reg and mem, store in AX (AA55)

	; XOR reg,mem+8b
	XOR DX, [BX+DI+02]	; XOR reg and mem+8b, store in DX (AA55)

	; XOR reg,mem+16b
	INC BP			; Set BP to 1
	XOR DX, [BP+0x0100]	; XOR reg and mem+16b, store in DX (55aa)

	; XOR reg, reg
	XOR AX, AX	; XOR 2 registers (00), check flags
	HLT		; Stop execution