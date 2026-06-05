package com.ronin.llm.dto;

public class ChatResponse {
    private String response;
    private String modelUsed;
    private boolean modelSwitched;
    private String previousModel;
    private boolean editsApplied;

    public ChatResponse(String response, String modelUsed, boolean modelSwitched, String previousModel, boolean editsApplied) {
        this.response = response;
        this.modelUsed = modelUsed;
        this.modelSwitched = modelSwitched;
        this.previousModel = previousModel;
        this.editsApplied = editsApplied;
    }

    public String getResponse() { return response; }
    public String getModelUsed() { return modelUsed; }
    public boolean isModelSwitched() { return modelSwitched; }
    public String getPreviousModel() { return previousModel; }
    public boolean isEditsApplied() { return editsApplied; }
}
