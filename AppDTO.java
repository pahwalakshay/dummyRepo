package pahwa;

import java.util.List

public class AppDTO {
private String appName;
private CategoryDTO categoryDTO;

public AppDTO(String appName, CategoryDTO categoryDTO){
        this.appName = appName;
        this.categoryDTO = categoryDTO;
        }
public getAppName(){
    return appName;
}
public void setAppName(String appName){
    this.appName = appName;
    }
public CategoryDTO getCategoryDTO(){
    return categoryDTO;
    }
public void setCategoryDTO(CategoryDTO categoryDTO){
    this.categoryDTO = categoryDTO;
}
 @Override
     public String toString() {
          return "AppDTO{" +
                  "appName='" + appName + '\'' +
                  ", categoryDTO=" + categoryDTO +
                  '}';
     }
}
