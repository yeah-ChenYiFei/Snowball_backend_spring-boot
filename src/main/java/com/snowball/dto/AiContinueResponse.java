package com.snowball.dto;

public class AiContinueResponse {
    private String continuation;
    private String model;
    private int tokensUsed;

    public AiContinueResponse() {}

    public AiContinueResponse(String continuation, String model, int tokensUsed) {
        this.continuation = continuation;
        this.model = model;
        this.tokensUsed = tokensUsed;
    }

    public String getContinuation() { return continuation; }
    public void setContinuation(String continuation) { this.continuation = continuation; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }
}
