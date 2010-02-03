; AND_EbGb (opcode 20), Logical byte-sized AND of memory/register (destination) and register (source)
; b8 ff ff a3 00 00 b8 00 00 b9 55 aa 20 08 a0 00 00 20 69 01
; a0 01 00 45 20 ae 00 01 a0 01 01 20 c8 f4
start:
	; Load memory with pre-arranged values 55AA
	MOV AX, 0xFFFF
	MOV [0000], AX
	MOV AX, 0x0000

	; AND mem,reg
	MOV CX, 0xAA55	      ; Move AA55 into CX
	AND [BX+SI], CL       ; OR reg and mem, store in memory
	MOV AL, [0000]	      ; Retrieve result from memory (result = 55)

	; AND mem+8b,reg
	AND [BX+DI+01], CH	  ; OR reg and mem+8b, store in memory
	MOV AL, [0001]		  ; Retrieve result from memory (result = aa)

	; AND mem+16b,reg
	INC BP			      ; Set BP to 1
	AND [BP+0x0100], CH	  ; OR reg and mem+16b, store in memory
	MOV AL, [0x0101]	  ; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; AND reg, reg
	AND AL, CL	          ; AND 2 registers (result = 00), check flags
	HLT		              ; Stop execution