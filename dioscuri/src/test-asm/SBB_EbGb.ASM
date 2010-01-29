; SBB_EbGb (opcode 18), subtract register (source) + CF from memory/register (destination)
; b8 7f 80 a3 00 00 b8 00 01 ba 7f ff f5 18 30 a0 00 00 18 59 01
; a0 01 00 45 18 96 00 01 a0 01 01 18 e0 18 d8 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x807F
	MOV [0000], AX
	MOV AX, 0x0100
	MOV DX, 0xFF7F

	; SBB mem,reg
	CMC			; Set carry flag
	SBB [BX+SI], DH ; 7F - (FF + CF), test AF flag
	MOV AL, [0000]	; Display result

	; SBB mem+8b,reg
	SBB [BX+DI+01], BL	; 80 - (00 + CF), test OF, SF, AF, PF flags
	MOV AL, [0001]		; Display result

	; SBB mem+16b,reg
	INC BP			; Set BP to 1
	SBB [BP+0x0100], DL	; 7F - 7F, test ZF
	MOV AL, [0x0101]	; Display result

	; SBB reg, reg
	SBB AL, AH	; 00 - 01, test CF, SBB, overflow
	SBB AL, BL	; FF - (00 + CF), test CF, PF
	HLT