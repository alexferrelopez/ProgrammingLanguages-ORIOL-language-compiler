package backEnd.targetCode;

import java.io.IOException;
import java.util.Map;

public class MipsTemplateRenderer implements MIPSRenderer {

    private static final String TEMPLATES_DIR = "templates/";
    private Boolean debug = false;
    private Map<String,String> templateCache = new java.util.HashMap<>();

    public MipsTemplateRenderer(Boolean debug) {
        this.debug = debug;
    }

    public MipsTemplateRenderer() {
        this.debug = false;
    }

    @Override
    public String render(String templatePath, Map<String, String> placeholders) {
        try {
            String templateContent = loadTemplate(templatePath);
            return renderTemplate(templateContent, placeholders);
        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Handle error appropriately
        }
    }

    private String loadTemplate(String templatePath) throws IOException {
        return templateCache.compute(templatePath, (k,v) -> {
            try {
                return loadTemplateFromFile(templatePath);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        });
    }

    private String loadTemplateFromFile(String templatePath) throws IOException {
        //read from classpath, so the templates are in the resources folder
        var resource = this.getClass().getClassLoader().getResourceAsStream(TEMPLATES_DIR + templatePath);
        if(resource == null) {
            throw new IOException("Template not found: " + templatePath);
        }
        return new String(resource.readAllBytes());

    }

    private String renderTemplate(String templateContent, Map<String, String> placeholders) {
        return renderTemplate(templateContent, placeholders, this.debug);
    }

    private String renderTemplate(String templateContent, Map<String, String> placeholders, Boolean debug) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            templateContent = templateContent.replace("{{ " + entry.getKey() + " }}", entry.getValue());
        }
        //trim template
        templateContent = templateContent.trim();
        //check it it ends with a newline
        if(!templateContent.endsWith("\n")) {
            templateContent += "\n";
        }

        if(!debug) {
            //replace all comments
            templateContent = templateContent.replaceAll("#.*", "");
            //remove all empty lines
            templateContent = templateContent.replaceAll("(?m)^[ \t]*\r?\n",    "");
        }
        else{
            //print all to console
            System.out.println(templateContent);
        }
        return templateContent;
    }
}
