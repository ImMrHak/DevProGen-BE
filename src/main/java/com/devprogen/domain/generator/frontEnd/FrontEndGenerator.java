package com.devprogen.domain.generator.frontEnd;

import com.devprogen.domain.project.model.Project;
import com.devprogen.domain.user.model.User;
import com.devprogen.infrastructure.utility.Utility;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FrontEndGenerator {
    // TO CHANGE
    private static final String DEFAULT_TEMPLATE_DIR = "C:\\Users\\Unknown\\IdeaProjects\\DevProGen-BE\\src\\main\\java\\com\\devprogen\\domain\\generator\\frontEnd\\templates";
    private static final String TEMPLATE_DIR = "D:\\TotalTemplates\\projectFrontEndTemplate";
    private static final String GENERATED_DIR = "D:\\GeneratedProjects";
    private String baseDir;

    private User projectOwner;
    private Project generatedProject;

    public FrontEndGenerator() {
        this.baseDir = GENERATED_DIR + "\\" + "Project_FrontEnd_" + Utility.generateRandomAlphanumeric(10);
    }

    public File generateFrontEndProject(InputStream yamlInputStream) throws IOException {
        // Copy the template directory to the new project directory
        copyDirectory(Paths.get(TEMPLATE_DIR), Paths.get(baseDir));

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(yamlInputStream);

        // Parsing entities
        List<Map<String, Object>> entities = (List<Map<String, Object>>) config.get("entities");

        for (Map<String, Object> entityConfig : entities) {
            String entityName = (String) entityConfig.get("name");
            boolean inheritsFromUser = false;
            List<Map<String, Boolean>> inheritanceControlList = (List<Map<String, Boolean>>) entityConfig.get("inheritanceControl");
            if (inheritanceControlList != null) {
                inheritsFromUser = inheritanceControlList.stream()
                        .anyMatch(control -> control.getOrDefault("inheritance", false));
            }

            // Parsing fields
            List<Map<String, Object>> fieldsList = (List<Map<String, Object>>) entityConfig.get("fields");
            Map<String, String> fields = fieldsList.stream()
                    .collect(Collectors.toMap(
                            field -> (String) field.get("name"),
                            field -> (String) field.get("type")
                    ));

            // Collecting required fields
            Map<String, Boolean> requiredFields = fieldsList.stream()
                    .collect(Collectors.toMap(
                            field -> (String) field.get("name"),
                            field -> field.getOrDefault("required", false).equals(true)
                    ));

            // Parsing relationships
            List<Map<String, Object>> relationshipsList = (List<Map<String, Object>>) entityConfig.get("relationships");
            Map<String, String> relationships = relationshipsList.stream()
                    .collect(Collectors.toMap(
                            rel -> (String) rel.get("name"),
                            rel -> (String) rel.get("type")
                    ));

            // Collecting required relationships
            Map<String, Boolean> requiredRelationships = relationshipsList.stream()
                    .collect(Collectors.toMap(
                            rel -> (String) rel.get("name"),
                            rel -> rel.getOrDefault("required", false).equals(true)
                    ));

            // Generate module, routing, service, and component for the entity
            generateModule(entityName, fields, relationships);
            generateRoutingModule(entityName);
            generateService(entityName);
            generateComponent(entityName, fields, relationships, requiredFields, requiredRelationships, inheritsFromUser);
            generateModel(entityName, fields, relationships, requiredFields, requiredRelationships, inheritsFromUser);
        }

        // Generate app module and app routing module
        generateAppModule();
        generateAppRoutingModule(entities);

        // Zip the project directory
        File zipFile = zipDirectory(baseDir);

        // Delete the project directory
        deleteDirectoryRecursively(new File(baseDir));

        return zipFile;
    }

    private void generateModule(String entityName, Map<String, String> fields, Map<String, String> relationships) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ModuleTemplate.txt")));
        String content = template.replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase());

        writeFile(baseDir + "\\src\\app\\" + entityName.toLowerCase() + "\\" + entityName.toLowerCase() + ".module.ts", content);
    }

    private void generateModel(String entityName, Map<String, String> fields, Map<String, String> relationships, Map<String, Boolean> requiredFields, Map<String, Boolean> requiredRelationships, boolean inheritsFromUser) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ModelTemplate.txt")));

        String entityFields = fields.entrySet().stream()
                .map(entry -> entry.getKey() + "!: " + "any;")
                .collect(Collectors.joining("\n"));

        List<String> imports = relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(entry -> {
                    String capitalizedEntity = capitalizeFirstLetter(entry.getKey());
                    return "import {" + capitalizedEntity + "} from './" + entry.getKey().toLowerCase() + ".model';";
                })
                .collect(Collectors.toList());

        String entityImports = String.join("\n", imports);

        entityFields += relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(entry -> {
                    String capitalizedEntity = capitalizeFirstLetter(entry.getKey());
                    return entry.getKey() + ": " + capitalizedEntity + " = new " + capitalizedEntity + "();";
                })
                .collect(Collectors.joining("\n"));

        if (inheritsFromUser) {
            entityFields = getUserAttributes() + entityFields;
        }

        String content = template.replace("{EntityName}", entityName)
                .replace("{EntityFields}", entityFields)
                .replace("{EntityImports}", entityImports);

        writeFile(baseDir + "\\src\\app\\models\\" + entityName.toLowerCase() + ".model.ts", content);
    }

    private String getUserAttributes() {
        return  "firstName!: string;\n" +
                "lastName!: string;\n" +
                "email!: string;\n" +
                "userName!: string;\n" +
                "password!: string;\n";
    }

    private void generateRoutingModule(String entityName) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\RoutingModuleTemplate.txt")));
        String content = template.replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase());

        writeFile(baseDir + "\\src\\app\\" + entityName.toLowerCase() + "\\" + entityName.toLowerCase() + "-routing.module.ts", content);
    }

    private void generateService(String entityName) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ServiceTemplate.txt")));
        String content = template.replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase());

        writeFile(baseDir + "\\src\\app\\services\\" + entityName.toLowerCase() + ".service.ts", content);
    }

    private void generateComponent(String entityName, Map<String, String> fields, Map<String, String> relationships, Map<String, Boolean> requiredFields, Map<String, Boolean> requiredRelationships, boolean inheritsFromUser) throws IOException {
        String htmlTemplate = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ComponentHTMLTemplate.txt")));
        String tsTemplate = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ComponentTemplate.txt")));
        String cssTemplate = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ComponentCssTemplate.txt")));

        // Generate form fields for HTML template
        String entityFormFields = fields.entrySet().stream()
                .map(entry -> "<div class=\"form-group\">\n<label>" + entry.getKey() + "</label>\n<input [(ngModel)]=\"selected" + entityName + "." + entry.getKey() + "\" name=\"" + entry.getKey() + "\" " + (requiredFields.get(entry.getKey()) ? "required" : "") + " />\n</div>\n")
                .collect(Collectors.joining("\n"));

        entityFormFields += relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(entry -> {
                    String relationshipName = entry.getKey();
                    return "<div class=\"form-group\">\n<label>" + relationshipName + " ID</label>\n<input [(ngModel)]=\"selected" + entityName + "." + relationshipName + ".id\" name=\"" + relationshipName + "Id\" " + (requiredRelationships.get(relationshipName) ? "required" : "") + " />\n</div>\n";
                })
                .collect(Collectors.joining("\n"));

        if (inheritsFromUser) {
            entityFormFields = getUserFormFields(entityName) + entityFormFields;
        }

        // Generate table headers for HTML template
        String entityTableHeaders = fields.keySet().stream()
                .map(field -> "<th>" + field + "</th>")
                .collect(Collectors.joining("\n"));

        entityTableHeaders += relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(field -> "<th>" + field.getKey() + " ID</th>")
                .collect(Collectors.joining("\n"));

        if (inheritsFromUser) {
            entityTableHeaders = getUserTableHeaders() + entityTableHeaders;
        }

        // Generate table fields for HTML template
        String entityTableFields = fields.keySet().stream()
                .map(field -> "<td>{{ " + entityName.toLowerCase() + "." + field + " }}</td>")
                .collect(Collectors.joining("\n"));

        entityTableFields += relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(field -> "<td>{{ " + entityName.toLowerCase() + "." + field.getKey() + "?.id }}</td>")
                .collect(Collectors.joining("\n"));

        if (inheritsFromUser) {
            entityTableFields = getUserTableFields(entityName.toLowerCase()) + entityTableFields;
        }

        // Replace placeholders in the HTML template
        String htmlContent = htmlTemplate.replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase())
                .replace("{EntityFormFields}", entityFormFields)
                .replace("{EntityTableHeaders}", entityTableHeaders)
                .replace("{EntityTableFields}", entityTableFields);

        // Generate fields for TypeScript template
        String entityFields = fields.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + ";")
                .collect(Collectors.joining("\n"));

        entityFields += relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(entry -> entry.getKey() + ": " + capitalizeFirstLetter(entry.getKey()) + ";")
                .collect(Collectors.joining("\n"));

        if (inheritsFromUser) {
            entityFields = getUserFields() + entityFields;
        }

        // Generate imports for relationships in TypeScript template
        String entityImports = relationships.entrySet().stream()
                .filter(entry -> entry.getValue().equals("ManyToOne") || entry.getValue().equals("OneToOne"))
                .map(entry -> "import {" + capitalizeFirstLetter(entry.getKey()) + "} from '../models/" + entry.getKey().toLowerCase() + ".model';")
                .collect(Collectors.joining("\n"));

        // Replace placeholders in the TypeScript template
        String tsContent = tsTemplate.replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase())
                .replace("{EntityFields}", entityFields)
                .replace("{EntityImports}", entityImports);

        // Write files to the respective paths
        String cssContent = cssTemplate;
        writeFile(baseDir + "\\src\\app\\" + entityName.toLowerCase() + "\\" + entityName.toLowerCase() + ".component.html", htmlContent);
        writeFile(baseDir + "\\src\\app\\" + entityName.toLowerCase() + "\\" + entityName.toLowerCase() + ".component.ts", tsContent);
        writeFile(baseDir + "\\src\\app\\" + entityName.toLowerCase() + "\\" + entityName.toLowerCase() + ".component.css", cssContent);
    }

    private String getUserFormFields(String entityName) {
        return  "<div class=\"form-group\">\n<label>First Name</label>\n<input [(ngModel)]=\"selected" + entityName + ".firstName\" name=\"firstName\" required />\n</div>\n" +
                "<div class=\"form-group\">\n<label>Last Name</label>\n<input [(ngModel)]=\"selected" + entityName + ".lastName\" name=\"lastName\" required />\n</div>\n" +
                "<div class=\"form-group\">\n<label>Email</label>\n<input [(ngModel)]=\"selected" + entityName + ".email\" name=\"email\" required />\n</div>\n" +
                "<div class=\"form-group\">\n<label>Username</label>\n<input [(ngModel)]=\"selected" + entityName + ".userName\" name=\"userName\" required />\n</div>\n" +
                "<div class=\"form-group\">\n<label>Password</label>\n<input [(ngModel)]=\"selected" + entityName + ".password\" name=\"password\" required />\n</div>\n";
    }

    private String getUserTableHeaders() {
        return "<th>ID User</th>\n<th>First Name</th>\n<th>Last Name</th>\n<th>Email</th>\n<th>Username</th>\n<th>Password</th>\n";
    }

    private String getUserTableFields(String entityName) {
        return "<td>{{ " + entityName + ".id }}</td>\n" +
                "<td>{{ " + entityName + ".firstName }}</td>\n" +
                "<td>{{ " + entityName + ".lastName }}</td>\n" +
                "<td>{{ " + entityName + ".email }}</td>\n" +
                "<td>{{ " + entityName + ".userName }}</td>\n" +
                "<td>{{ " + entityName + ".password }}</td>\n";
    }

    private String getUserFields() {
        return "firstName: string;\nlastName: string;\nemail: string;\nuserName: string;\npassword: string;\n";
    }

    private void generateAppModule() throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\AppModuleTemplate.txt")));
        String content = template.replace("{EntityComponentImports}", "")
                .replace("{EntityComponents}", "")
                .replace("{EntityServiceImports}", "")
                .replace("{EntityServices}", "");

        writeFile(baseDir + "\\src\\app\\app.module.ts", content);
    }

    private void generateAppRoutingModule(List<Map<String, Object>> entities) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\AppRoutingModuleTemplate.txt")));
        String entityLazyRoutes = entities.stream()
                .map(entity -> {
                    String entityName = (String) entity.get("name");
                    return "{ path: '" + entityName.toLowerCase() + "', loadChildren: () => import('./" + entityName.toLowerCase() + "/" + entityName.toLowerCase() + ".module').then(m => m." + entityName + "Module) }";
                })
                .collect(Collectors.joining(",\n  "));

        String content = template.replace("{EntityLazyRoutes}", entityLazyRoutes);
        writeFile(baseDir + "\\src\\app\\app-routing.module.ts", content);
    }

    private void writeFile(String path, String content) throws IOException {
        Files.createDirectories(Paths.get(path).getParent());
        Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private File zipDirectory(String dirPath) throws IOException {
        String zipFilePath = dirPath + ".zip";
        Path zipPath = Paths.get(zipFilePath);

        // Ensure the ZIP file is created
        if (Files.notExists(zipPath)) {
            Files.createFile(zipPath);
        }

        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Path sourcePath = Paths.get(dirPath);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString().replace("\\", "/"));
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Error zipping file: " + path);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error creating ZIP file");
            e.printStackTrace();
        }

        return new File(zipFilePath);
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void deleteDirectoryRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteDirectoryRecursively(subFile);
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        }
    }
}
