; AND_EvGv (opcode 21), Logical word-sized AND of memory/register (destination) and register (source)
; b8 ff ff a3 00 00 a3 02 00 b8 00 00 b9 55 aa 21 08 a1 00 00 b8 00 00
; 21 49 02 a1 02 00 b8 00 00 45 21 8e 00 01 a1 01 01 21 d8 f4
start:
	; Load memory with pre-arranged values FFFF FFFF
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0x0000

	; AND mem,reg
	MOV CX, 0xAA55	; Move AA55 into CX
	AND [BX+SI], CX ; AND reg and mem, storee in memory
	MOV AX, [0000]	; Retrieve result from memory (result = aa55)
	MOV AX, 0x0000	; Reset AX

	; AND mem+8b,reg
	AND [BX+DI+02], CX	; AND reg and mem+8b, store in memory
	MOV AX, [0002]		; Retrieve result from memory (result = aa55)
	MOV AX, 0x0000		; Reset AX

	; AND mem+16b,reg
	INC BP			; Set BP to 1
	AND [BP+0x0100], CX	; AND reg and mem+16b, store in memory
	MOV AX, [0x0101]	; Retrieve result from memory (result = aa55) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; AND reg, reg
	AND AX, BX	; AND 2 registers (result = 0000), check flags
	HLT		; Stop execution