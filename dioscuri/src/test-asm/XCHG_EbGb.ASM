; XCHG_EbGb (opcode 86), exchange contents of memory/register (destination) and register (source)
; b8 bb aa a3 00 00 b8 dd cc 86 00 a0 00 00 86 61 01 a0 01 00
; 45 86 86 00 01 a0 01 01 86 c5 f4
start:
	; Load memory with pre-arranged values
	MOV AX, 0xAABB
	MOV [0000], AX
	MOV AX, 0xCCDD

	; XCHG mem,reg
	XCHG [BX+SI], AL; XCHG reg and mem, store in memory
	MOV AL, [0000]	; Retrieve result from memory (result = bb)

	; XCHG mem+8b,reg
	XCHG [BX+DI+01], AH	; XCHG reg and mem+8b, store in memory
	MOV AL, [0001]		; Retrieve result from memory (result = aa)

	; XCHG mem+16b,reg
	INC BP			; Set BP to 1
	XCHG [BP+0x0100], AL	; XCHG reg and mem+16b, store in memory
	MOV AL, [0x0101]	; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; XCHG reg, reg
	XCHG AL, CH	; XCHG 2 registers (result = 00)
	HLT		; Stop execution