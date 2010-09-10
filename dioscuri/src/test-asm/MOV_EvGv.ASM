; MOV_EvGv (opcode 89), copy contents of memory/register (destination) from register (source)
; b9 bb aa ba dd cc 89 08 a1 00 00 89 51 02 a1 02 00 45 89 8e 00 01
; a1 01 01 89 c3 f4
start:
	; Load memory with pre-arranged values
	MOV CX, 0xAABB
	MOV DX, 0xCCDD

	; MOV mem,reg
	MOV [BX+SI], CX ; MOV reg and mem, store in memory
	MOV AX, [0000]	; Retrieve result from memory (result = aabb)

	; MOV mem+8b,reg
	MOV [BX+DI+02], DX	; MOV reg and mem+8b, store in memory
	MOV AX, [0002]		; Retrieve result from memory (result = ccdd)

	; MOV mem+16b,reg
	INC BP			; Set BP to 1
	MOV [BP+0x0100], CX	; MOV reg and mem+16b, store in memory
	MOV AX, [0x0101]	; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; MOV reg, reg
	MOV BX, AX	; MOV 2 registers
	HLT		; Stop execution