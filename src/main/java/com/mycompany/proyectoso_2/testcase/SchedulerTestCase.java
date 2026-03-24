package com.mycompany.proyectoso_2.testcase;

public class SchedulerTestCase {

    private final String testId;
    private final int initialHead;
    private final SchedulerTestRequest[] requests;
    private final int[] systemFilePositions;
    private final String[] systemFileNames;
    private final int[] systemFileBlocks;

    public SchedulerTestCase(
            String testId,
            int initialHead,
            SchedulerTestRequest[] requests,
            int[] systemFilePositions,
            String[] systemFileNames,
            int[] systemFileBlocks
    ) {
        if (testId == null || testId.isBlank()) {
            throw new IllegalArgumentException("El identificador del caso es obligatorio.");
        }
        this.testId = testId;
        this.initialHead = initialHead;
        this.requests = copyRequests(requests);
        this.systemFilePositions = copyInts(systemFilePositions);
        this.systemFileNames = copyStrings(systemFileNames);
        this.systemFileBlocks = copyInts(systemFileBlocks);
    }

    public String getTestId() {
        return testId;
    }

    public int getInitialHead() {
        return initialHead;
    }

    public SchedulerTestRequest[] getRequests() {
        return copyRequests(requests);
    }

    public int[] getSystemFilePositions() {
        return copyInts(systemFilePositions);
    }

    public String[] getSystemFileNames() {
        return copyStrings(systemFileNames);
    }

    public int[] getSystemFileBlocks() {
        return copyInts(systemFileBlocks);
    }

    private SchedulerTestRequest[] copyRequests(SchedulerTestRequest[] source) {
        if (source == null) {
            return new SchedulerTestRequest[0];
        }
        SchedulerTestRequest[] copy = new SchedulerTestRequest[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }

    private int[] copyInts(int[] source) {
        if (source == null) {
            return new int[0];
        }
        int[] copy = new int[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }

    private String[] copyStrings(String[] source) {
        if (source == null) {
            return new String[0];
        }
        String[] copy = new String[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }
}
