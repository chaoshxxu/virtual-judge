package judge.tool;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;


/**
 * 销毁临时文件
 * @author Isun
 *
 */
public class TempFileCleaner {

    public void clean() throws IOException {
        String relativePath = (String) ApplicationContainer.serveletContext.getAttribute("DataPath");
        String path = ApplicationContainer.serveletContext.getRealPath(relativePath);
        FileUtils.deleteDirectory(new File(path));
    }

}
