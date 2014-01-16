package judge.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class ZipUtil {
	
	public static void zip(File destZip, File sourceDir) {
		List<String> pathList = new ArrayList<String>();
		generateFileList(sourceDir, pathList);
		
		int startIndex = sourceDir.getAbsolutePath().length() + 1;
		byte[] buffer = new byte[1024];
		try {
			FileOutputStream fos = new FileOutputStream(destZip);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (String path : pathList) {
				ZipEntry ze = new ZipEntry(path.substring(startIndex));
				zos.putNextEntry(ze);
				FileInputStream in = new FileInputStream(path);
				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				in.close();
			}
			zos.closeEntry();
			zos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	public static void generateFileList(File node, List<String> fileList) {
		if (node.isFile()) {
			fileList.add(node.getAbsolutePath());
		} else if (node.isDirectory()) {
			for (String filename : node.list()) {
				generateFileList(new File(node, filename), fileList);
			}
		}
	}
}