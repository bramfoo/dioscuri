; XOR_EvGv (opcode 31), Logical word-sized XOR of memory/register (destination) and register (source)
; b8 ff ff a3 00 00 a3 02 00 b8 00 00 b9 55 aa 31 08 a1 00 00 b8 00 00
; 31 49 02 a1 02 00 b8 00 00 45 31 8e 00 01 a1 01 01 31 c0 f4
start:
	; Load memory with pre-arranged values FFFF FFFF
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0x0000

	; XOR mem,reg
	MOV CX, 0xAA55	; Move AA55 into CX
	XOR [BX+SI], CX ; XOR reg and mem, store in memory
	MOV AX, [0000]	; Retrieve result from memory (result = 55aa)
	MOV AX, 0x0000	; Clear AX

	; XOR mem+8b,reg
	XOR [BX+DI+02], CX	; XOR reg and mem+8b, store in memory
	MOV AX, [0002]		; Retrieve result from memory (result = aa55)
	MOV AX, 0x0000	; Clear AX

	; XOR mem+16b,reg
	INC BP			; Set BP to 1
	XOR [BP+0x0100], CX	; XOR reg and mem+16b, store in memory
	MOV AX, [0x0101]	; Retrieve result from memory (result = ff) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; XOR reg, reg
	XOR AX, AX	; XOR 2 registers (result = 0000), check flags
	HLT		; Stop execution