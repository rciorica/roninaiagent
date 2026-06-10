package com.ronin.llm.dto;

import com.ronin.llm.LLMEditInstruction;

import java.util.List;

public class ChatResponse {
    private String response;
    private String modelUsed;
    private boolean modelSwitched;
    private String previousModel;
    private boolean editsApplied;
    private String editPayload;
    private List<LLMEditInstruction> edits;

    public ChatResponse(String response, String modelUsed, boolean modelSwitched, String previousModel, boolean editsApplied, String editPayload, List<LLMEditInstruction> edits) {
        this.response = response;
        this.modelUsed = modelUsed;
        this.modelSwitched = modelSwitched;
        this.previousModel = previousModel;
        this.editsApplied = editsApplied;
        this.editPayload = editPayload;
        this.edits = edits;
    }

    public String getResponse() { return response; }
    public String getModelUsed() { return modelUsed; }
    public boolean isModelSwitched() { return modelSwitched; }
    public String getPreviousModel() { return previousModel; }
    public boolean isEditsApplied() { return editsApplied; }
    public String getEditPayload() { return editPayload; }
    public List<LLMEditInstruction> getEdits() { return edits; }
}
