; TEST_EvGv (opcode 85), Logical word-sized comparison (AND) of memory/register (destination) and register (source)
; b8 ff ff a3 00 00 a3 02 00 b8 a8 55 b9 55 aa 85 08 85 41 02 45
; 85 8e 00 01 85 d8 f4
start:
	; Load memory with pre-arranged values FFFF FFFF
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0x55A8	; Move AA55 into CX
	MOV CX, 0xAA55	; Move AA55 into CX

	; TEST mem,reg
	TEST [BX+SI], CX; TEST reg and mem, store in memory

	; TEST mem+8b,reg
	TEST [BX+DI+02], AX	; TEST reg and mem+8b, store in memory

	; TEST mem+16b,reg
	INC BP			; Set BP to 1
	TEST [BP+0x0100], CX	; TEST reg and mem+16b, store in memory

	; TEST reg, reg
	TEST AX, BX	; TEST 2 registers (result = 0000), check flags
	HLT		; Stop execution