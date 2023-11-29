import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class UpdateRecordNames {

    private static final Pattern recordPattern = Pattern.compile("(?<=interface)((.|\\s)*?)(?=})", Pattern.CASE_INSENSITIVE);
    private static final Pattern recordFieldPattern = Pattern.compile("\\sget[A-Z].+;", Pattern.CASE_INSENSITIVE);


    public static void main(String[] args) throws IOException {
        String currentDirectory = UpdateRecordNames.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();

        try (Stream<Path> fileWalkerStream = Files.walk(Paths.get(currentDirectory))) {
            fileWalkerStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .forEach(path -> {
                        String fileContent = readFileContent(path);
                        if (fileContent.contains("@GeneratedPersistence")) {
                            String updatedContent = updateRecords(fileContent);
                            updateFileContents(path, updatedContent);
                        }
                    });
        }

    }

    private static String updateRecords(String fileContent) {
        // getting intterContent as recordPattern regex will clash with the text 'interface'
        String innerContent = fileContent.substring(fileContent.indexOf("{"));
        
        Matcher recordMatcher = recordPattern.matcher(innerContent);
        String updatedContent = fileContent;

        while (recordMatcher.find()) {
            String record = recordMatcher.group();
            String updatedRecord = updateRecordFields(record);
            updatedContent = updatedContent.replace(record, updatedRecord);

        }

        return updatedContent;
    }

    private static String updateRecordFields(String record) {
        Matcher recordFieldMatcher = recordFieldPattern.matcher(record);
        String updatedRecord = record;

        while (recordFieldMatcher.find()) {
            String recordField = recordFieldMatcher.group();
            String updatedRecordField = " " + recordField.substring(4, 5).toLowerCase() + recordField.substring(5);
            updatedRecord = updatedRecord.replace(recordField, updatedRecordField);
        }

        return updatedRecord;
    }

    private static String readFileContent(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateFileContents(Path path, String content) {
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
