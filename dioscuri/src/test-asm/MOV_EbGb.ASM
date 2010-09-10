; MOV_EbGb (opcode 88), copy contents of memory/register (destination) and register (source)
; b9 bb aa 88 08 a0 00 00 88 69 01 a0 01 00 45 88 8e 00 01 a0 01 01
; 88 c4 f4
start:
	; Load memory with pre-arranged values
	MOV CX, 0xAABB

	; MOV mem,reg
	MOV [BX+SI], CL ; MOV reg and mem, store in memory
	MOV AL, [0000]	; Retrieve result from memory (result = bb)

	; MOV mem+8b,reg
	MOV [BX+DI+01], CH	; MOV reg and mem+8b, store in memory
	MOV AL, [0001]		; Retrieve result from memory (result = aa)

	; MOV mem+16b,reg
	INC BP			; Set BP to 1
	MOV [BP+0x0100], CL	; MOV reg and mem+16b, store in memory
	MOV AL, [0x0101]	; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; MOV reg, reg
	MOV AH, AL	; MOV 2 registers (result = 00)
	HLT		; Stop execution