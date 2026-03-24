package com.mycompany.proyectoso_2.simulation;

import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SchedulerTestCaseSmokeTest {

    private SchedulerTestCaseSmokeTest() {
    }

    public static void main(String[] args) throws IOException {
        SimulationController controller = new SimulationController();
        Path testCaseFile = Files.createTempFile("scheduler-case", ".json");
        Files.writeString(testCaseFile, """
                {
                  "test_id": "P1",
                  "initial_head": 50,
                  "requests": [
                    {"pos": 95, "op": "READ"},
                    {"pos": 180, "op": "READ"},
                    {"pos": 34, "op": "READ"},
                    {"pos": 119, "op": "DELETE"},
                    {"pos": 11, "op": "READ"},
                    {"pos": 123, "op": "UPDATE"},
                    {"pos": 62, "op": "UPDATE"},
                    {"pos": 64, "op": "READ"}
                  ],
                  "system_files": {
                    "11": {"name": "boot_sect.bin", "blocks": 2},
                    "34": {"name": "readme.txt", "blocks": 1},
                    "62": {"name": "script.py", "blocks": 8},
                    "64": {"name": "style.css", "blocks": 6},
                    "95": {"name": "config.sys", "blocks": 4},
                    "119": {"name": "image_01.png", "blocks": 12},
                    "123": {"name": "data_log.csv", "blocks": 28},
                    "180": {"name": "video_clip.mp4", "blocks": 52}
                  }
                }
                """);

        assertPolicy(controller, testCaseFile, SchedulingPolicy.FIFO,
                new int[]{95, 180, 34, 119, 11, 123, 62, 64});
        assertPolicy(controller, testCaseFile, SchedulingPolicy.SSTF,
                new int[]{62, 64, 34, 11, 95, 119, 123, 180});
        assertPolicy(controller, testCaseFile, SchedulingPolicy.SCAN,
                new int[]{62, 64, 95, 119, 123, 180, 34, 11});
        assertPolicy(controller, testCaseFile, SchedulingPolicy.C_SCAN,
                new int[]{62, 64, 95, 119, 123, 180, 11, 34});

        Files.deleteIfExists(testCaseFile);
        System.out.println("OK: pruebas integradas de casos JSON del scheduler completadas.");
    }

    private static void assertPolicy(
            SimulationController controller,
            Path testCaseFile,
            SchedulingPolicy policy,
            int[] expectedOrder
    ) throws IOException {
        controller.loadTestCase(testCaseFile);
        controller.setSchedulingPolicy(policy);
        controller.resumeScheduler();

        assertTrue(controller.waitUntilIdle(10_000),
                "El caso " + policy + " debe terminar dentro del tiempo esperado.");
        assertArrayEquals(expectedOrder, controller.getCompletedRequestPositionsSnapshot(),
                "El orden ejecutado debe coincidir con " + policy + ".");
        assertEquals(8, controller.buildAllocationRows().length,
                "El caso cargado debe exponer los ocho system files.");
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String message) {
        if (expected.length != actual.length) {
            throw new IllegalStateException(message
                    + " Longitud esperada: " + expected.length
                    + ", recibida: " + actual.length + ".");
        }
        for (int index = 0; index < expected.length; index++) {
            if (expected[index] != actual[index]) {
                throw new IllegalStateException(message
                        + " Diferencia en indice " + index
                        + ": esperado " + expected[index]
                        + ", recibido " + actual[index] + ".");
            }
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new IllegalStateException(message
                + " Esperado: " + expected
                + ", recibido: " + actual + ".");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
