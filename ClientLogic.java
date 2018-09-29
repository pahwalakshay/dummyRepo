package pahwa;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClientLogic {
    public static void main(String[] args) {
        ClientLogic cl = new ClientLogic();
     final   Map<String,List<CategoryDTO>> catMap = new HashMap<>();

         List<AppDTO>  returnedList = cl.getFile("resouces/read.txt");
         System.out.println(returnedList);
         List<CategoryDTO> cat1 = new ArrayList<>();
        List<CategoryDTO> cat2 = new ArrayList<>();
        List<CategoryDTO> cat3 = new ArrayList<>();
        for (AppDTO  e: returnedList) {
             if(e.getAppName().equals("Mail App")){
                 cat1.add(e.getCategoryDTO());
                 catMap.put(e.getAppName(),cat1);


             }
              else if(e.getAppName().equals("Video Call App")){
                 cat2.add(e.getCategoryDTO());
                 catMap.put(e.getAppName(),cat2);
             }
              else if(e.getAppName().equals("Chat App")){
                 cat3.add(e.getCategoryDTO());
                 catMap.put(e.getAppName(),cat3);
             }
        }
        for(Map.Entry<String,List<CategoryDTO>> map : catMap.entrySet()){
            System.out.println(map);
        }


    }

    private List<AppDTO> getFile(String fileName) {

        StringBuilder result = new StringBuilder("");
         result.trimToSize();
        List<AppDTO> appList =  new ArrayList<>();
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] element = line.split(",");

                CategoryDTO catDTO = new CategoryDTO(element[1].trim(),element[2].trim());
                AppDTO dto = new AppDTO(element[0], catDTO);
                appList.add(dto);
            //    result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return appList;

    }
}
