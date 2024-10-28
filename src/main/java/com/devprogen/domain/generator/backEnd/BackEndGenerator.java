package com.devprogen.domain.generator.backEnd;

import com.devprogen.application.attribute.AttributeService;
import com.devprogen.application.entity.EntityService;
import com.devprogen.application.project.ProjectService;
import com.devprogen.domain.attribute.model.Attribute;
import com.devprogen.domain.entity.model.Entity;
import com.devprogen.domain.project.model.Project;
import com.devprogen.domain.user.model.User;
import com.devprogen.infrastructure.utility.Utility;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackEndGenerator {
    // TO CHANGE
    private static final String DEFAULT_TEMPLATE_DIR = "C:\\Users\\Unknown\\IdeaProjects\\DevProGen-BE\\src\\main\\java\\com\\devprogen\\domain\\generator\\backEnd\\templates";
    private static final String TEMPLATE_DIR = "D:\\TotalTemplates\\projectBackEndTemplate";
    private static final String GENERATED_DIR = "D:\\GeneratedProjects";
    private String baseDir;

    private User projectOwner;
    private Project generatedProject;

    public ProjectService projectSI;
    public EntityService entitySI;
    public AttributeService attributeSI;
    public Utility utility;

    public BackEndGenerator(User user, String projectName, Boolean isBEOnly) {
        projectOwner = user;
        generatedProject = new Project();
        generatedProject.setName(projectName);
        generatedProject.setUser(user);

        if(isBEOnly){
            baseDir = GENERATED_DIR + "\\" + "Project_BackEnd_" + Utility.generateRandomAlphanumeric(10);
            generatedProject.setProjectDirectoryFolder(baseDir + ".zip");
        }
        else{
            generatedProject.setProjectDirectoryFolder(GENERATED_DIR + "\\" + generatedProject.getName() + ".zip");
            baseDir = GENERATED_DIR + "\\" + "Project_BackEnd_" + Utility.generateRandomAlphanumeric(10);
        }


    }

    public File generateBackEndProject(InputStream yamlInputStream) throws IOException {
        // Saving The Created Project In The Database
        if (!Objects.isNull(projectSI.createMyProject(generatedProject)))
            System.out.println("Project created, inserted in the database by: " + projectOwner.getFirstName() + " " + projectOwner.getLastName());

        // Copy the template directory to the new project directory
        copyDirectory(Paths.get(TEMPLATE_DIR), Paths.get(baseDir));

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(yamlInputStream);

        // Parsing entities
        List<Map<String, Object>> entities = (List<Map<String, Object>>) config.get("entities");

        Map<String, List<Map<String, Boolean>>> accessControls = new HashMap<>();

        for (Map<String, Object> entityConfig : entities) {
            String entityName = (String) entityConfig.get("name");

            // Parsing fields
            List<Map<String, String>> fieldsList = (List<Map<String, String>>) entityConfig.get("fields");
            Map<String, String> fields = fieldsList.stream()
                    .collect(Collectors.toMap(field -> field.get("name"), field -> field.get("type")));

            // Parsing relationships
            List<Map<String, String>> relationshipsList = (List<Map<String, String>>) entityConfig.get("relationships");

            // Parsing access control
            List<Map<String, Boolean>> accessControlList = new ArrayList<>();
            for (Map<String, Object> role : (List<Map<String, Object>>) entityConfig.get("accessControl")) {
                Map<String, Boolean> roleMap = new HashMap<>();
                roleMap.put("allowed", Boolean.parseBoolean(role.get("allowed").toString()));
                accessControlList.add(roleMap);
            }
            accessControls.put(entityName, accessControlList);

            // Parsing inheritance control
            List<Map<String, Boolean>> inheritanceControlList = (List<Map<String, Boolean>>) entityConfig.get("inheritanceControl");
            boolean inheritance = inheritanceControlList != null && inheritanceControlList.stream()
                    .anyMatch(control -> control.get("inheritance"));

            // Generate entity with correct parameters
            generateEntity(entityName, fields, relationshipsList, inheritance);

            // Generate other components
            generateRepository(entityName);
            generateServiceImpl(entityName, inheritance);
            generateController(entityName);
        }

        // Generate security configuration for all entities
        generateSecurityConfig(accessControls);

        // Generate application properties
        generateApplicationProperty(generatedProject.getName());

        // Generate project application
        // generateProjectApplication(entities);

        // Zip the project directory
        File zipFile = zipDirectory(baseDir);

        // Delete the project directory
        deleteDirectoryRecursively(new File(baseDir));

        // Return zipped project directory
        return zipFile;
    }

    public void generateApplicationProperty(String projectName) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ApplicationPropertiesTemplate.txt")));
        String content = template.replace("{databaseName}", projectName);
        writeFile(baseDir + "/src/main/resources/application.properties", content);
    }

    public void generateEntity(String entityName, Map<String, String> fields, List<Map<String, String>> relationships, boolean inheritance) throws IOException {
        // Saving The Created Entity In The Database
        Entity entity = new Entity();
        entity.setName(entityName);
        entity.setInheritFromUser(inheritance);
        entity.setProject(generatedProject);
        if (!Objects.isNull(entitySI.createMyEntity(entity)))
            System.out.println("Entity created, inserted in the database by: " + projectOwner.getFirstName() + " " + projectOwner.getLastName() + " for this project: " + generatedProject.getName());

        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\EntityTemplate.txt")));
        String entityFields = generateFields(fields, entity);

        // Convert List<Map<String, String>> to Map<String, Map<String, String>>
        Map<String, Map<String, String>> relationshipMap = relationships.stream()
                .collect(Collectors.toMap(
                        rel -> rel.get("name"),
                        rel -> {
                            Map<String, String> details = new HashMap<>();
                            details.put("type", rel.get("type"));
                            details.put("targetEntity", rel.get("targetEntity"));
                            return details;
                        }
                ));

        String entityRelationships = generateRelationships(relationshipMap, entity);
        String inheritanceStr = inheritance ? "extends User" : "";

        // Conditionally include the id field
        String idField = inheritance ? "" : "@Id @GeneratedValue(strategy = GenerationType.IDENTITY)\nprivate Long id" + ";";

        String content = template.replace("{EntityName}", entityName)
                .replace("{idField}", idField)
                .replace("{EntityFields}", entityFields)
                .replace("{Relationships}", entityRelationships)
                .replace("{inheritance}", inheritanceStr);
        writeFile(baseDir + "/src/main/java/com/generated/project/dao/" + entityName + ".java", content);
    }

    public void generateRepository(String entityName) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\RepositoryTemplate.txt")));
        String content = template.replace("{EntityName}", entityName);
        writeFile(baseDir + "/src/main/java/com/generated/project/services/" + entityName + "Repository.java", content);
    }

    public void generateServiceImpl(String entityName, boolean inheritsFromUser) throws IOException {
        System.out.println("Inherets : " + inheritsFromUser + " TABLE NAME : " + entityName);
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ServiceImplTemplate.txt")));

        String setIdMethod = "{entityName}.setId(id);";

        String content = template.replace("{setIdMethod}", setIdMethod)
                .replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase());

        writeFile(baseDir + "/src/main/java/com/generated/project/services/" + entityName + "ServiceImpl.java", content);
    }

    public void generateController(String entityName) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ControllerTemplate.txt")));
        String content = template.replace("{EntityName}", entityName)
                .replace("{entityName}", entityName.toLowerCase());
        writeFile(baseDir + "/src/main/java/com/generated/project/presentation/" + entityName + "Controller.java", content);
    }

    public void generateSecurityConfig(Map<String, List<Map<String, Boolean>>> accessControls) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\SecurityConfigTemplate.txt")));

        // Use a Map to keep track of requestMatchers to avoid duplicates
        Map<String, String> requestMatchersMap = new HashMap<>();

        for (Map.Entry<String, List<Map<String, Boolean>>> entry : accessControls.entrySet()) {
            String entityName = entry.getKey();
            List<Map<String, Boolean>> roles = entry.getValue();

            // Default roleMatcher to "USER"
            String roleMatcher = "USER";

            // Check for "ADMIN" role and its allowed status
            for (Map<String, Boolean> role : roles) {
                Boolean isAdminAllowed = role.get("allowed");
                if (isAdminAllowed != null && isAdminAllowed) {
                    roleMatcher = "ADMIN";
                    break;
                }
            }

            if(roleMatcher.equals("USER")){
                // Construct the matcher string for this endpoint
                String matcher = String.format(".requestMatchers(\"/api/%s/**\").hasRole(\"%s\")", entityName.toLowerCase(), "ADMIN");

                // Only add the matcher if it's not already present
                requestMatchersMap.putIfAbsent(matcher, matcher);
            }

            // Construct the matcher string for this endpoint
            String matcher = String.format(".requestMatchers(\"/api/%s/**\").hasRole(\"%s\")", entityName.toLowerCase(), roleMatcher);

            // Only add the matcher if it's not already present
            requestMatchersMap.putIfAbsent(matcher, matcher);
        }

        // Join all the unique requestMatchers into one string
        String requestMatchers = requestMatchersMap.values().stream()
                .collect(Collectors.joining("\n        "));

        // Replace {RequestMatchers} placeholder with the constructed matchers string
        String content = template.replace("{RequestMatchers}", requestMatchers);

        writeFile(baseDir + "/src/main/java/com/generated/project/security/SecurityConfig.java", content);
    }

    public String generateFields(Map<String, String> fields, Entity entity) {
        Attribute attributePrimary = new Attribute();
        attributePrimary.setForeignKey(false);
        attributePrimary.setPrimaryKey(true);
        attributePrimary.setName("id" + entity.getName());
        attributePrimary.setType("Long");
        attributePrimary.setEntity(entity);
        if (!Objects.isNull(attributeSI.createMyAttribute(attributePrimary)))
            System.out.println("Primary attribute created, inserted in the database by: " + projectOwner.getFirstName() + " " + projectOwner.getLastName() + " for this project: " + generatedProject.getName() + " and for this entity: " + entity.getName());

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append("private ").append(entry.getValue()).append(" ").append(entry.getKey()).append(";\n");

            // Saving The Created Attributes In The Database
            Attribute attribute = new Attribute();
            attribute.setForeignKey(false);
            attribute.setName(entry.getKey());
            attribute.setType(entry.getValue());
            attribute.setEntity(entity);

            if (!Objects.isNull(attributeSI.createMyAttribute(attribute)))
                System.out.println("Attribute created, inserted in the database by: " + projectOwner.getFirstName() + " " + projectOwner.getLastName() + " for this project: " + generatedProject.getName() + " and for this entity: " + entity.getName());
        }
        return sb.toString();
    }

    public String generateRelationships(Map<String, Map<String, String>> relationships, Entity entity) {
        Attribute attributePrimary = new Attribute();
        attributePrimary.setForeignKey(false);
        attributePrimary.setPrimaryKey(true);
        attributePrimary.setName("id" + entity.getName());
        attributePrimary.setType("Long");
        attributePrimary.setEntity(entity);
        if (!Objects.isNull(attributeSI.createMyAttribute(attributePrimary)))
            System.out.println("Primary attribute created, inserted in the database by: " + projectOwner.getFirstName() + " " + projectOwner.getLastName() + " for this project: " + generatedProject.getName() + " and for this entity: " + entity.getName());

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Map<String, String>> entry : relationships.entrySet()) {
            String relatedEntity = entry.getKey();
            Map<String, String> relationshipDetails = entry.getValue();
            String relationType = relationshipDetails.get("type");
            String targetEntity = relationshipDetails.get("targetEntity");

            switch (relationType) {
                case "OneToMany":
                    sb.append("@OneToMany(mappedBy = \"").append(entity.getName().toLowerCase()).append("\") @JsonIgnore\n")
                            .append("private List<").append(targetEntity).append("> ").append(relatedEntity.toLowerCase()).append("s;\n");
                    break;
                case "ManyToOne":
                    sb.append("@ManyToOne\n")
                            .append("@JoinColumn(name = \"").append(relatedEntity.toLowerCase()).append("_id\")\n")
                            .append("private ").append(targetEntity).append(" ").append(relatedEntity.toLowerCase()).append(";\n");
                    break;
                case "OneToOne":
                    sb.append("@OneToOne\n")
                            .append("@JoinColumn(name = \"").append(relatedEntity.toLowerCase()).append("_id\")\n")
                            .append("private ").append(targetEntity).append(" ").append(relatedEntity.toLowerCase()).append(";\n");
                    break;
                case "ManyToMany":
                    sb.append("@ManyToMany\n")
                            .append("@JoinTable(name = \"").append(entity.getName().toLowerCase()).append("_")
                            .append(relatedEntity.toLowerCase()).append("\", joinColumns = @JoinColumn(name = \"")
                            .append(entity.getName().toLowerCase()).append("_id\"), inverseJoinColumns = @JoinColumn(name = \"")
                            .append(relatedEntity.toLowerCase()).append("_id\")) @JsonIgnore\n")
                            .append("private List<").append(targetEntity).append("> ").append(relatedEntity.toLowerCase()).append("s;\n");
                    break;
            }

            // Saving The Created Attributes In The Database
            Attribute attribute = new Attribute();
            attribute.setName(relatedEntity);
            attribute.setType(targetEntity);
            attribute.setForeignKey(true);
            attribute.setReferencedKey(targetEntity);
            attribute.setRelationShipType(relationType);
            attribute.setEntity(entity);

            if (Objects.isNull(attributeSI.createMyAttribute(attribute))) System.out.println("Attribute (RelationShip => Referenced Key) created, inserted in the database by : " + projectOwner.getFirstName() + " " + projectOwner.getLastName() + " for this project : " + generatedProject.getName() + " and for this entity : " + entity.getName());
        }
        return sb.toString();
    }

    public void generateProjectApplication(List<Map<String, Object>> entities) throws IOException {
        String template = new String(Files.readAllBytes(Paths.get(DEFAULT_TEMPLATE_DIR + "\\ProjectApplicationTemplate.txt")));

        // Sort entities before generating insert statements
        List<Map<String, Object>> sortedEntities = sortEntities(entities);

        // Generating EntityServiceImplementations
        StringBuilder siBuilder = new StringBuilder();
        StringBuilder dataInsertBuilder = new StringBuilder();

        Map<String, String> entityVariableMap = new HashMap<>();

        for (Map<String, Object> entityConfig : sortedEntities) {
            String entityName = (String) entityConfig.get("name");
            String entityVar = String.valueOf(entityName.charAt(0)).toLowerCase();
            entityVariableMap.put(entityName, entityVar);

            siBuilder.append(entityName).append("ServiceImpl ").append(entityVar).append("SI = ac.getBean(").append(entityName).append("ServiceImpl.class);\n");

            // Assuming each entity has a default constructor and setters for the fields
            dataInsertBuilder.append(entityName).append(" ").append(entityVar).append(" = new ").append(entityName).append("();\n");

            // Generating field inserts
            List<Map<String, String>> fieldsList = (List<Map<String, String>>) entityConfig.get("fields");
            for (Map<String, String> field : fieldsList) {
                String fieldName = field.get("name");
                dataInsertBuilder.append(entityVar).append(".set").append(capitalize(fieldName)).append("(/* nzid les valeurs hna */);\n");
            }

            // Generating relationship inserts
            List<Map<String, String>> relationships = (List<Map<String, String>>) entityConfig.get("relationships");
            for (Map<String, String> relationship : relationships) {
                String relType = relationship.get("type");
                String relTarget = relationship.get("targetEntity");
                String relName = relationship.get("name");

                switch (relType) {
                    case "ManyToOne":
                        dataInsertBuilder.append(entityVar).append(".set").append(capitalize(relName)).append("(").append(entityVariableMap.get(relTarget)).append(");\n");
                        break;
                    case "OneToOne":
                        dataInsertBuilder.append(entityVar).append(".set").append(capitalize(relName)).append("(").append(entityVariableMap.get(relTarget)).append(");\n");
                        break;
                    case "OneToMany":
                        dataInsertBuilder.append(entityVariableMap.get(relTarget)).append(".get").append(capitalize(relName)).append("s().add(").append(entityVar).append(");\n");
                        break;
                    case "ManyToMany":
                        dataInsertBuilder.append(entityVar).append(".get").append(capitalize(relName)).append("s().add(").append(entityVariableMap.get(relTarget)).append(");\n");
                        break;
                }
            }

            dataInsertBuilder.append(entityVar).append("SI.save(").append(entityVar).append(");\n\n");
        }

        // Replace placeholders with the generated content
        String content = template
                .replace("{EntityServiceImplementations}", siBuilder.toString())
                .replace("{EntityDataToInsert}", dataInsertBuilder.toString());

        writeFile(baseDir + "/src/main/java/com/generated/project/ProjectApplication.java", content);
    }

    private String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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

        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath));
             Stream<Path> paths = Files.walk(Paths.get(dirPath))) {
            paths.filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(Paths.get(dirPath).relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Error zipping file: " + path, e);
                        }
                    });
        }

        return new File(zipFilePath);
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

    private List<Map<String, Object>> sortEntities(List<Map<String, Object>> entities) {
        List<Map<String, Object>> sortedEntities = new ArrayList<>();
        Map<String, Map<String, Object>> entityMap = new HashMap<>();

        for (Map<String, Object> entity : entities) {
            entityMap.put((String) entity.get("name"), entity);
        }

        Set<String> visited = new HashSet<>();

        for (Map<String, Object> entity : entities) {
            sortEntityHelper(entity, visited, sortedEntities, entityMap);
        }

        return sortedEntities;
    }

    private void sortEntityHelper(Map<String, Object> entity, Set<String> visited, List<Map<String, Object>> sortedEntities, Map<String, Map<String, Object>> entityMap) {
        String entityName = (String) entity.get("name");
        if (visited.contains(entityName)) {
            return;
        }
        visited.add(entityName);

        List<Map<String, String>> relationships = (List<Map<String, String>>) entity.get("relationships");
        for (Map<String, String> relationship : relationships) {
            String targetEntityName = relationship.get("targetEntity");
            if ("ManyToOne".equals(relationship.get("type")) || "OneToOne".equals(relationship.get("type")) || "ManyToMany".equals(relationship.get("type"))) {
                sortEntityHelper(entityMap.get(targetEntityName), visited, sortedEntities, entityMap);
            }
        }

        sortedEntities.add(entity);
    }
}
