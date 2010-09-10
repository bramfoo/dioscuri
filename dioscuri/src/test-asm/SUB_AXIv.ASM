; SUB_AX_Iv (opcode 2D), subtract immediate word from AX
; 2d 00 80 2d 03 10 2d fd 6f 2d 02 10 2d 02 10 2d fc 60 2d 00 ff 2d 00 ff f4
start:
	SUB ax, 0x8000	; Test OF, SF, CF
	SUB ax, 0x1003	; Test OF, SF, AF, PF flags
	SUB ax, 0x6FFD	; Test ZF
	SUB ax, 0x1002	; Test AF, CF, overflow
	SUB ax, 0x1002	; Test CF, PF
	SUB ax, 0x60FC	; Prepare special case
	SUB ax, 0xFF00	; Test OF, CF
	SUB ax, 0xFF00	; Test OF, CF
	HLT