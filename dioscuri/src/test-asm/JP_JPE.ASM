; JP / JPE (opcode 7A), jump on parity / parity even
; 7a fe 7a fc 40 40 40 7a f7
start:
	JP start	; Jump if parity (fail)
	JPE start	; Show ASM instruction has identical bytecode
	INC ax		; Increment ax to set parity flag
	INC ax		; Increment ax to set parity flag
	INC ax		; Increment ax to set parity flag
	JP start	; Jump if not signed (fail)