package org.sterl.llmpeon.exception;

/**
 * Signals a tool execution error from a broken provider response (stream truncation,
 * JSON parse failure) that must abort the tool loop immediately — no retry, no re-parse.
 */
public class ToolExecutionPermanentFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ToolExecutionPermanentFailureException(Throwable cause) {
        super(cause);
    }
}
