; XOR_GbEb (opcode 32), Logical byte-sized XOR of register (destination) and memory/register (source)
; b8 aa 55 a3 00 00 b8 ff ff 32 00 32 61 01 45 32 a6 00 01 30 c0 f4
start:
	; Load memory with pre-arranged values 55AA
	MOV AX, 0x55AA
	MOV [0000], AX
	MOV AX, 0xFFFF	; Move ffff into AX

	; XOR reg,mem
	XOR AL, [BX+SI] ; XOR reg and mem, store in AL (55)

	; XOR reg,mem+8b
	XOR AH, [BX+DI+01]	; XOR reg and mem+8b, store in AH (aa)

	; XOR reg,mem+16b
	INC BP			; Set BP to 1
	XOR AH, [BP+0x0100]	; XOR reg and mem+16b, store in AL (55)

	; XOR reg, reg
	XOR AL, AL	; XOR 2 registers (00), check flags
	HLT		; Stop execution