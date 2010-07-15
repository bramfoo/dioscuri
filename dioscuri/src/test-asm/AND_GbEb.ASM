; AND_GbEb (opcode 22), Logical byte-sized AND of register (destination) and memory/register (source)
; b8 80 a9 a3 00 00 b8 aa 55 22 00 22 61 01 45 22 a6 00 01 20 e0 f4
start:
	; Load memory with pre-arranged values 55AA
	MOV AX, 0xA980
	MOV [0000], AX
	MOV AX, 0x55AA

	; AND reg,mem
	AND AL, [BX+SI] ; AND reg and mem, store in AL (80)

	; AND reg,mem+8b
	AND AH, [BX+DI+01]	; AND reg and mem+8b, store in AH (01)

	; AND reg,mem+16b
	INC BP			; Set BP to 1
	AND AH, [BP+0x0100]	; AND reg and mem+16b, store in AL (80)

	; AND reg, reg
	AND AL, AH	; AND 2 registers (00), check flags
	HLT		; Stop execution