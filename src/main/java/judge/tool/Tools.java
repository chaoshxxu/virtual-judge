package judge.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * 公用工具类
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public class Tools {

    /**
     * html转义
     * @param str
     * @return
     */
    public static String toHTMLChar(String str) {
        if (str == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);
            if (c == '&'){
                sb.append("&#38;");
            } else if (c == '"'){
                sb.append("&#34;");
            } else if (c == '\''){
                sb.append("&#39;");
            } else if (c == '<'){
                sb.append("&lt;");
            } else if (c == '>'){
                sb.append("&gt;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * html反转义
     * @param str
     * @return
     */
    public static String toPlainChar(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("&#38;", "&").replaceAll("&#34;", "\"").replaceAll("&#39;", "'").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
    }

//    /**
//     * 去掉html中的js部分
//     * @return
//     */
//    public static String dropScript(String html) {
//        return html == null ? null : html.replaceAll("(?i)(?<=(\\b|java))script\\b", "ｓcript");
//    }

    /**
     * 获取Excel中第一个sheet内容
     * @param xls
     * @return
     * @throws IndexOutOfBoundsException
     * @throws BiffException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String[][] splitCellsFromExcel(File xls) throws IndexOutOfBoundsException, BiffException, FileNotFoundException, IOException {
        Sheet rs = Workbook.getWorkbook(new FileInputStream(xls)).getSheet(0);
        ArrayList<String[]> tmpContent = new ArrayList<String[]>();
        for (int i = 0; i < rs.getRows(); i++) {
            List row = new ArrayList<String>();
            for (int j = 0; j < rs.getColumns(); j++) {
                row.add(rs.getCell(j, i).getContents().trim());
            }
            tmpContent.add((String[]) row.toArray(new String[0]));
        }
        return tmpContent.toArray(new String[0][]);
    }

    /**
     * 按照reg解析text,获取第i组
     * @param text
     * @param reg
     * @param i
     * @return
     */
    public static String regFind(String text, String reg, int i){
        Matcher m = Pattern.compile(reg, Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.group(i) : "";
    }

    /**
     * 按照reg解析text,获取第1组
     * @param text
     * @param reg
     * @param i
     * @return
     */
    public static String regFind(String text, String reg){
        return regFind(text, reg, 1);
    }

    /**
     * 按照reg解析text,获取第i组(区分大小写)
     * @param text
     * @param reg
     * @param i
     * @return
     */
    public static String regFindCaseSensitive(String text, String reg, int i){
        Matcher m = Pattern.compile(reg).matcher(text);
        return m.find() ? m.group(i) : "";
    }

    /**
     * 按照reg解析text,获取第1组(区分大小写)
     * @param text
     * @param reg
     * @param i
     * @return
     */
    public static String regFindCaseSensitive(String text, String reg){
        return regFindCaseSensitive(text, reg, 1);
    }

//    /**
//     * 全角转半角
//     * @param input 全角字符串.
//     * @return 半角字符串
//     */
//    public static String toDBC(String input) {
//        char c[] = input.toCharArray();
//        for (int i = 0; i < c.length; i++) {
//            if (c[i] == '\u3000') {
//                c[i] = ' ';
//            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
//                c[i] = (char) (c[i] - 65248);
//            }
//        }
//        return new String(c);
//    }

    /**
     * 将一段时间从毫秒数转换为通用的表示方式
     * @param length 毫秒数
     * @param hasDay 转换结果是否含有"天"
     * @return
     */
    public static String transPeriod(Long length, boolean hasDay){
        long d = length / 86400000;
        long h = (hasDay ? length % 86400000 : length) / 3600000;
        long m = length % 3600000 / 60000;
        long s = length % 60000 / 1000;
        return (hasDay && d > 0 ? (d + "天") : "") + h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    /**
     * 为shjs寻找正确的class
     * @param srcLang
     * @return
     */
    public static String findClass4SHJS(String srcLang) {
        srcLang = " " + StringUtils.lowerCase(srcLang) + " ";
        if (srcLang.contains("c++") || srcLang.contains("cpp") || srcLang.contains("g++")){
            return "sh-cpp";
        } else if (srcLang.contains(" c ") || srcLang.contains("gcc")){
            return "sh-c";
        } else if (srcLang.contains("c#")){
            return "sh-csharp";
        } else if (srcLang.contains("java ")){
            return "sh-java";
        } else if (srcLang.contains("pascal") || srcLang.contains("fpc")){
            return "sh-pascal";
        } else if (srcLang.contains("tcl")){
            return "sh-tcl";
        } else if (srcLang.contains("scala")){
            return "sh-scala";
        } else if (srcLang.contains("perl")){
            return "sh-perl";
        } else if (srcLang.contains("python")){
            return "sh-python";
        } else if (srcLang.contains("ruby")){
            return "sh-ruby";
        } else if (srcLang.contains("php")){
            return "sh-php";
        } else if (srcLang.contains("prolog")){
            return "sh-prolog";
        } else if (srcLang.contains("javascript")){
            return "sh-javascript";
        } else {
            return "sh-c";
        }
    }
    
    public static <T> List<Class<? extends T>> findSubClasses(String packagePath, Class<T> parentClass) throws ClassNotFoundException {
        List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(parentClass));

        Set<BeanDefinition> components = provider.findCandidateComponents(packagePath);
        for (BeanDefinition component : components) {
            Class<? extends T> clazz = (Class<? extends T>) Class.forName(component.getBeanClassName());
            result.add(clazz);
        }
        return result;
    }
    
}
