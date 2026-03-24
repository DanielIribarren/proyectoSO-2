package com.mycompany.proyectoso_2.persistence;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;
import com.mycompany.proyectoso_2.persistence.json.JsonArrayValue;
import com.mycompany.proyectoso_2.persistence.json.JsonNumberValue;
import com.mycompany.proyectoso_2.persistence.json.JsonObjectValue;
import com.mycompany.proyectoso_2.persistence.json.JsonStringValue;
import com.mycompany.proyectoso_2.persistence.json.JsonValue;
import com.mycompany.proyectoso_2.persistence.json.SimpleJsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimulationStateRepository {

    public void save(Path path, SimulationSaveData saveData) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("La ruta de guardado es obligatoria.");
        }
        Files.writeString(path, buildJson(saveData), StandardCharsets.UTF_8);
    }

    public SimulationSaveData load(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("La ruta de carga es obligatoria.");
        }
        String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
        JsonObjectValue root = new SimpleJsonParser(jsonContent).parseObject();
        return new SimulationSaveData(
                UserMode.valueOf(root.getString("mode")),
                SchedulingPolicy.valueOf(root.getString("policy")),
                root.getInt("head"),
                root.has("currentUser") ? root.getString("currentUser") : "daniel",
                parseDirectories(root.getArray("directories")),
                parseFiles(root.getArray("files"))
        );
    }

    private SavedDirectory[] parseDirectories(JsonArrayValue arrayValue) {
        SavedDirectory[] directories = new SavedDirectory[arrayValue.size()];
        for (int index = 0; index < arrayValue.size(); index++) {
            JsonObjectValue objectValue = (JsonObjectValue) arrayValue.get(index);
            directories[index] = new SavedDirectory(
                    objectValue.getString("path"),
                    objectValue.getString("owner"),
                    EntryVisibility.valueOf(objectValue.getString("visibility"))
            );
        }
        return directories;
    }

    private SavedFile[] parseFiles(JsonArrayValue arrayValue) {
        SavedFile[] files = new SavedFile[arrayValue.size()];
        for (int index = 0; index < arrayValue.size(); index++) {
            JsonObjectValue objectValue = (JsonObjectValue) arrayValue.get(index);
            files[index] = new SavedFile(
                    objectValue.getString("path"),
                    objectValue.getString("owner"),
                    EntryVisibility.valueOf(objectValue.getString("visibility")),
                    objectValue.getInt("size"),
                    parseIntArray(objectValue.getArray("blocks")),
                    objectValue.getInt("colorId"),
                    objectValue.has("ioPosition") ? objectValue.getInt("ioPosition") : -1
            );
        }
        return files;
    }

    private int[] parseIntArray(JsonArrayValue arrayValue) {
        int[] values = new int[arrayValue.size()];
        for (int index = 0; index < arrayValue.size(); index++) {
            values[index] = ((JsonNumberValue) arrayValue.get(index)).getValue();
        }
        return values;
    }

    private String buildJson(SimulationSaveData saveData) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendStringProperty(json, "mode", saveData.getUserMode().name(), true);
        appendStringProperty(json, "policy", saveData.getSchedulingPolicy().name(), true);
        appendNumberProperty(json, "head", saveData.getHeadPosition(), true);
        appendStringProperty(json, "currentUser", saveData.getCurrentUser(), true);
        appendDirectories(json, saveData.getDirectories());
        json.append(",\n");
        appendFiles(json, saveData.getFiles());
        json.append("\n}\n");
        return json.toString();
    }

    private void appendDirectories(StringBuilder json, SavedDirectory[] directories) {
        json.append("  \"directories\": [\n");
        for (int index = 0; index < directories.length; index++) {
            SavedDirectory directory = directories[index];
            json.append("    {\n");
            appendStringProperty(json, "path", directory.getPath(), true, 6);
            appendStringProperty(json, "owner", directory.getOwner(), true, 6);
            appendStringProperty(json, "visibility", directory.getVisibility().name(), false, 6);
            json.append("\n    }");
            if (index < directories.length - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]");
    }

    private void appendFiles(StringBuilder json, SavedFile[] files) {
        json.append("  \"files\": [\n");
        for (int index = 0; index < files.length; index++) {
            SavedFile file = files[index];
            json.append("    {\n");
            appendStringProperty(json, "path", file.getPath(), true, 6);
            appendStringProperty(json, "owner", file.getOwner(), true, 6);
            appendStringProperty(json, "visibility", file.getVisibility().name(), true, 6);
            appendNumberProperty(json, "size", file.getSizeInBlocks(), true, 6);
            appendIntArrayProperty(json, "blocks", file.getBlocks(), true, 6);
            appendNumberProperty(json, "colorId", file.getColorId(), true, 6);
            appendNumberProperty(json, "ioPosition", file.getIoPosition(), false, 6);
            json.append("\n    }");
            if (index < files.length - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]");
    }

    private void appendStringProperty(
            StringBuilder json,
            String name,
            String value,
            boolean trailingComma
    ) {
        appendStringProperty(json, name, value, trailingComma, 2);
    }

    private void appendStringProperty(
            StringBuilder json,
            String name,
            String value,
            boolean trailingComma,
            int indent
    ) {
        json.append(" ".repeat(indent))
                .append('"').append(name).append("\": ")
                .append('"').append(escape(value)).append('"');
        if (trailingComma) {
            json.append(',');
        }
        json.append('\n');
    }

    private void appendNumberProperty(
            StringBuilder json,
            String name,
            int value,
            boolean trailingComma
    ) {
        appendNumberProperty(json, name, value, trailingComma, 2);
    }

    private void appendNumberProperty(
            StringBuilder json,
            String name,
            int value,
            boolean trailingComma,
            int indent
    ) {
        json.append(" ".repeat(indent))
                .append('"').append(name).append("\": ")
                .append(value);
        if (trailingComma) {
            json.append(',');
        }
        json.append('\n');
    }

    private void appendIntArrayProperty(
            StringBuilder json,
            String name,
            int[] values,
            boolean trailingComma,
            int indent
    ) {
        json.append(" ".repeat(indent))
                .append('"').append(name).append("\": [");
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                json.append(", ");
            }
            json.append(values[index]);
        }
        json.append(']');
        if (trailingComma) {
            json.append(',');
        }
        json.append('\n');
    }

    private String escape(String value) {
        String escaped = value.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        return escaped.replace("\n", "\\n");
    }
}
