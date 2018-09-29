package pahwa;

public class CategoryDTO {
    private String moduleName;
    private String version;

    public CategoryDTO(String moduleName, String version) {
        this.moduleName = moduleName;
        this.version = version;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "moduleName='" + moduleName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
