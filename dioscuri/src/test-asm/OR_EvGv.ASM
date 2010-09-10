; OR_EvGv (opcode 09), Logical word-sized OR of memory/register (destination) and register (source)
; b8 aa 55 a3 00 00 a3 02 00 b8 00 00 b9 55 aa 09 08 a1 00 00 40
; 09 49 02 a1 02 00 40 45 09 8e 00 01 a1 01 01 40 09 d8 f4
start:
	; Load memory with pre-arranged values 55AA 55AA
	MOV AX, 0x55AA
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0x0000

	; OR mem,reg
	MOV CX, 0xAA55	; Move AA55 into CX
	OR [BX+SI], CX	; OR reg and mem, store in memory
	MOV AX, [0000]	; Retrieve result from memory (result = ffff)
	INC AX		; Reset AX

	; OR mem+8b,reg
	OR [BX+DI+02], CX	; OR reg and mem+8b, store in memory
	MOV AX, [0002]		; Retrieve result from memory (result = ffff)
	INC AX			; Reset AX

	; OR mem+16b,reg
	INC BP			; Set BP to 1
	OR [BP+0x0100], CX	; OR reg and mem+16b, store in memory
	MOV AX, [0x0101]	; Retrieve result from memory (result = ff) [NOTE: ACCESSING BYTE 2 OF OWN CODE]
	INC AX			; Reset AX

	; OR reg, reg
	OR AX, BX	; OR 2 registers (result = 0000), check flags
	HLT		; Stop execution