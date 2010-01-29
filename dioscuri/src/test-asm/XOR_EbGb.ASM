; XOR_EbGb (opcode 30), Logical byte-sized XOR of memory/register (destination) and register (source)
; b8 ff ff a3 00 00 b8 00 00 b9 55 aa 30 08 a0 00 00 30 69 01
; a0 01 00 45 30 8e 00 01 a0 01 01 30 e8 f4
start:
	; Load memory with pre-arranged values FFFF
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV AX, 0x0000

	; XOR mem,reg
	MOV CX, 0xAA55	; Move AA55 into CX
	XOR [BX+SI], CL ; XOR reg and mem, store in memory
	MOV AL, [0000]	; Retrieve result from memory (result = aa)

	; XOR mem+8b,reg
	XOR [BX+DI+01], CH	; XOR reg and mem+8b, store in memory
	MOV AL, [0001]		; Retrieve result from memory (result = 55)

	; XOR mem+16b,reg
	INC BP			; Set BP to 1
	XOR [BP+0x0100], CL	; XOR reg and mem+16b, store in memory
	MOV AL, [0x0101]	; Retrieve result from memory (result = ff) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; XOR reg, reg
	XOR AL, CH	; XOR 2 registers (result = 0000), check flags
	HLT		; Stop execution