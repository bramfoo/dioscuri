; SUB_EbGb (opcode 28), subtract memory/register (destination) from register (source)
; b8 85 05 a2 00 00 ba 02 7b 28 20 a0 00 00 a2 02 00 28 61 02 a0 02 00
; a2 01 01 45 28 b6 00 01 a0 01 01 28 d0 28 d0 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x0585
	MOV [0000], AL
	MOV DX, 0x7B02

	; SUB mem,reg
	SUB [BX+SI], AH ; 85 - 05, test SF, PF flags
	MOV AL, [0000]	; Display result

	; SUB mem+8b,reg
	MOV [0002], AL
	SUB [BX+DI+02], AH	; 80 - 05, test OF, SF, AF, PF flags
	MOV AL, [0002]		; Display result

	; SUB mem+16b,reg
	MOV [0x0101], AL
	INC BP			; Set BP to 1
	SUB [BP+0x0100], DH	; 7B - 7B, test ZF
	MOV AL, [0x0101]     ; Display result

	; CMP reg, reg
	SUB AL, DL	; 00 - 02, test CF, overflow
	SUB AL, DL	; FE - 02, test CF, PF
	HLT