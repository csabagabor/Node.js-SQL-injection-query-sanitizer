package replacemysql;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        List<String> all = new ArrayList<>();

        Pattern p = Pattern.compile(".*connection.query[(]([^,]*),.*");

        try {
            File myObj = new File("src/main/java/replacemysql/my");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                all.add(data);
                if (data.contains("connection.query")) {
                    Matcher m = p.matcher(data);
                    if (m.matches()) {
                        //System.out.println(m.group(1));
                        String group = m.group(1).trim();

                    }
                }
            }


            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        Pattern pp1 = Pattern.compile("[^']*('[\\s]*\"[\\s]*[+][\\s]*[^+]+[\\s]*[+][\\s]*\"[\\s]*').*");
        Pattern pp2 = Pattern.compile("[^\"]*(\"[\\s]*'[\\s]*[+][\\s]*[^+]+[\\s]*[+][\\s]*'[\\s]*\").*");


        for (int i = 0; i < all.size(); i++) {
            String data = all.get(i);
            if (data.contains("connection.query")) {
                Matcher m = p.matcher(data);
                if (m.matches()) {
                    //System.out.println(m.group(1));
                    String group = m.group(1).trim();
                    //find group
                    //Pattern p2 = Pattern.compile(".*" + group + "[^=]*=[^\"]\"");

                    for (int j = i - 1; j >= 0; j--) {
                        String data2 = all.get(j);
                        if (data2.contains(group) && !data2.contains("console.log")) {
                            int i1 = data2.indexOf("\"");
                            int i2 = data2.lastIndexOf("\"");
                            if (i2 != -1 && (!data2.contains("'") || data2.indexOf("'") > i1)) {
                                String query = data2.substring(i1, i2 + 1);
                                String oldQuery = query;
                                //System.out.println(query);

                                List<String> params = new ArrayList<>();
                                while (true) {
                                    m = pp1.matcher(query);

                                    if (m.find()) {
                                        String rr = m.group(1);
                                        //System.out.println(rr); // = m.group(), the whole match value
                                        query = query.replaceFirst(Pattern.quote(rr), " ? ");
                                        //System.out.println(query);

                                        rr = rr.replaceAll("'", "")
                                                .replaceAll(" ", "")
                                                .replaceAll("[+]", "")
                                                .replaceAll("\"", "");
                                        //System.out.println(rr);
                                        params.add(rr);
                                    } else {
                                        break;
                                    }
                                }

                                data2 = data2.replace(oldQuery, query);
                                all.set(j, data2);

                                int indexOf = data.indexOf("connection.query");
                                int indOfPar = data.indexOf(",", indexOf);
                                String pars = String.join(",", params);
                                if(params.size() > 0) {
                                    data = data.substring(0, indOfPar + 1) + "[" + pars + "]," + data.substring(indOfPar + 1);
                                }
                                // data = data.replace("connection.query", "callCustomDb");
                                all.set(i, data);
                                break;
                            } else {
                                //simple quotes
                                i1 = data2.indexOf("'");
                                i2 = data2.lastIndexOf("'");

                                if (i2 != -1) {
                                    String query = data2.substring(i1, i2 + 1);
                                    String oldQuery = query;
                                    //System.out.println(query);

                                    List<String> params = new ArrayList<>();
                                    while (true) {
                                        m = pp2.matcher(query);

                                        if (m.find()) {
                                            String rr = m.group(1);
                                            //System.out.println(rr); // = m.group(), the whole match value
                                            query = query.replaceFirst(Pattern.quote(rr), " ? ");
                                            //System.out.println(query);

                                            rr = rr.replaceAll("'", "")
                                                    .replaceAll(" ", "")
                                                    .replaceAll("[+]", "")
                                                    .replaceAll("\"", "");
                                            //System.out.println(rr);
                                            params.add(rr);
                                        } else {
                                            break;
                                        }
                                    }

                                    data2 = data2.replace(oldQuery, query);
                                    all.set(j, data2);

                                    int indexOf = data.indexOf("connection.query");
                                    int indOfPar = data.indexOf(",", indexOf);
                                    String pars = String.join(",", params);
                                    if(params.size() > 0) {
                                        data = data.substring(0, indOfPar + 1) + "[" + pars + "]," + data.substring(indOfPar + 1);
                                    }
                                    //data = data.replace("connection.query", "callCustomDb");
                                    all.set(i, data);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (String s : all) {
            System.out.println(s);
        }

        
    }
}