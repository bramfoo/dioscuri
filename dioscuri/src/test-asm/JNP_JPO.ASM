; JNP / JPO (opcode 7B), jump not parity / parity odd
; 40 40 40 7b fb 7b f9 40 7b f6
start:
	INC ax		; Increment ax to set parity flag
	INC ax		; Increment ax to set parity flag
	INC ax		; Increment ax to set parity flag
	JNP start	; Jump if parity (fail)
	JPO start	; Show ASM instruction has identical bytecode
	INC ax		; Increment ax to set parity flag
	JNP start	; Jump if not signed (fail)