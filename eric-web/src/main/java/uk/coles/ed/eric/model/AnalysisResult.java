package uk.coles.ed.eric.model;

public class AnalysisResult {
    private String output;
    private boolean responseFound;
    private boolean apologiesExhausted;

    public AnalysisResult(String output, boolean responseFound, boolean apologiesExhausted) {
        this.output = output;
        this.responseFound = responseFound;
        this.apologiesExhausted = apologiesExhausted;
    }

    public String getOutput() {
        return output;
    }

    public boolean isResponseFound() {
        return responseFound;
    }

    public boolean isApologiesExhausted() {
        return apologiesExhausted;
    }
}
