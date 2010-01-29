; TEST_EbGb (opcode 84), Logical byte-sized comaprison (AND) of memory/register (destination) and register (source)
; b8 ff ff a3 00 00 b8 00 00 b9 54 aa 84 28 84 49 01 45 84 ae 00 01 84 c8 f4
start:
	; Load memory with pre-arranged values 55AA
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV AX, 0x0000
	MOV CX, 0xAA54	; Move AA54 into CX

	; TEST mem,reg
	TEST [BX+SI], CH; TEST reg and mem, store in memory (SF, PF)

	; TEST mem+8b,reg
	TEST [BX+DI+01], CL	; TEST reg and mem+8b, store in memory (SF, PF)

	; AND mem+16b,reg
	INC BP			; Set BP to 1
	TEST [BP+0x0100], CH	; TEST reg and mem+16b, store in memory (SF, PF)

	; TEST reg, reg
	TEST AL, CL	; TEST 2 registers (result = 00), check flags (ZF, SF, PF)
	HLT		; Stop execution