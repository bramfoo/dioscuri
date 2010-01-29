; OR_EbGb (opcode 08), Logical byte-sized OR of memory/register (destination) and register (source)
; b8 aa 55 a3 00 00 b8 00 00 b9 55 aa 08 08 a0 00 00 40 08 69 01
; a0 01 00 40 45 08 8e 00 01 a0 01 01 40 08 f8 f4
start:
	; Load memory with pre-arranged values 55AA
	MOV AX, 0x55AA
	MOV [0000], AX
	MOV AX, 0x0000

	; OR mem,reg
	MOV CX, 0xAA55	; Move AA55 into CX
	OR [BX+SI], CL	; OR reg and mem, store in memory
	MOV AL, [0000]	; Retrieve result from memory (result = ff)
	INC AX		; Reset AL

	; OR mem+8b,reg
	OR [BX+DI+01], CH	; OR reg and mem+8b, store in memory
	MOV AL, [0001]		; Retrieve result from memory (result = ff)
	INC AX			; Reset AL

	; OR mem+16b,reg
	INC BP			; Set BP to 1
	OR [BP+0x0100], CL	; OR reg and mem+16b, store in memory
	MOV AL, [0x0101]	; Retrieve result from memory (result = ff) [NOTE: ACCESSING BYTE 2 OF OWN CODE]
	INC AX			; Reset AL

	; OR reg, reg
	OR AL, BH	; OR 2 registers (result = 0000), check flags
	HLT		; Stop execution