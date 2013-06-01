package judge.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class ZipUtil {

	public static void zip(File zipFile, File dir) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		zip(out, dir, "");
		out.close();
	}

	private static void zip(ZipOutputStream out, File dir, String base) throws Exception {
		if (dir.isDirectory()) {
			File[] fl = dir.listFiles();
			out.putNextEntry(new ZipEntry(base + "/"));
			base = base.length() == 0 ? "" : base + "/";
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + fl[i].getName());
			}
		} else {
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(dir);
			int b;
//			System.out.println(base);
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			in.close();
		}
	}

}