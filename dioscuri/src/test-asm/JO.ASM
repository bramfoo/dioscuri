; Simple jump on overflow test
; Byte-code: 70 06 b8 ff 7f 40 eb f8 40 eb f5
start:
	JO oflow
	MOV ax, 0x7FFF
	INC ax
	JMP start
oflow:
	INC ax
	JMP start