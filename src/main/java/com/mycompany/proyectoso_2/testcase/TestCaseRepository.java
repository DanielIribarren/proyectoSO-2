package com.mycompany.proyectoso_2.testcase;

import com.mycompany.proyectoso_2.persistence.json.JsonArrayValue;
import com.mycompany.proyectoso_2.persistence.json.JsonObjectValue;
import com.mycompany.proyectoso_2.persistence.json.JsonProperty;
import com.mycompany.proyectoso_2.persistence.json.SimpleJsonParser;
import com.mycompany.proyectoso_2.process.OperationType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestCaseRepository {

    public SchedulerTestCase load(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("La ruta del caso de prueba es obligatoria.");
        }
        String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
        JsonObjectValue root = new SimpleJsonParser(jsonContent).parseObject();

        JsonArrayValue requestsArray = root.getArray("requests");
        SchedulerTestRequest[] requests = new SchedulerTestRequest[requestsArray.size()];
        for (int index = 0; index < requestsArray.size(); index++) {
            JsonObjectValue requestObject = (JsonObjectValue) requestsArray.get(index);
            requests[index] = new SchedulerTestRequest(
                    requestObject.getInt("pos"),
                    OperationType.valueOf(requestObject.getString("op"))
            );
        }

        JsonObjectValue systemFilesObject = root.getObject("system_files");
        int[] positions = new int[systemFilesObject.size()];
        String[] names = new String[systemFilesObject.size()];
        int[] blocks = new int[systemFilesObject.size()];
        for (int index = 0; index < systemFilesObject.size(); index++) {
            JsonProperty property = systemFilesObject.getPropertyAt(index);
            JsonObjectValue fileObject = (JsonObjectValue) property.getValue();
            positions[index] = Integer.parseInt(property.getName());
            names[index] = fileObject.getString("name");
            blocks[index] = fileObject.getInt("blocks");
        }

        return new SchedulerTestCase(
                root.getString("test_id"),
                root.getInt("initial_head"),
                requests,
                positions,
                names,
                blocks
        );
    }
}
