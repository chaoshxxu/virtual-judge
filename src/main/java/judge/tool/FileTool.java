package judge.tool;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.struts2.ServletActionContext;

public class FileTool {
	// 保存上传文件
	public static String saveUpload(File file, String fileName,
			String savePath, String signID) throws Exception {
		// 获得绝对路径
		String realPath = ServletActionContext.getServletContext().getRealPath(savePath);
		System.out.println(realPath);
		// 获取源文件后缀名
		String extendName = fileName.substring(fileName.lastIndexOf("."));
		System.out.println(extendName);
		// 获取系统时间并转成字符串
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String dateformat = format.format(date);
		// 生成新的文件名
		String realName = dateformat + extendName;
		// System.out.println(realPath + "\\" + realName);
		// 以realPath和realName建立一个输出流
		System.out.println(realPath + "/" + realName);
		FileOutputStream fos = new FileOutputStream(realPath + "\\" + realName);
		// 以上传文件建立一个输入流
		FileInputStream fis = new FileInputStream(file);
		// 保存文件
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = fis.read(b)) != -1) {
			fos.write(b, 0, len);
		}
		fis.close();
		fos.close();
		return realPath + "\\" + realName;
	}

	// 删除文件
	public static void fileDelete(String fileName) {
		fileName = ServletActionContext.getServletContext().getRealPath(
				fileName);
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
	}

	// 生成缩略图
	public static void scaleImage(String fromFileSrc,// 原始图片的路径
			String toFileSrc,// 转化后图片的路径
			int formatWidth,// 格式化图片的宽度
			int formatHeight) throws IOException {// 格式化图片的高度
		// 原始图片的实际路径
		String fromPath = ServletActionContext.getServletContext().getRealPath(fromFileSrc);
		// 转化后图片的路径
		String toPath = ServletActionContext.getServletContext().getRealPath(toFileSrc);
		File fi = new File(fromPath);
        File fo = new File(toPath);
        AffineTransform transform = new AffineTransform();
        BufferedImage bis = ImageIO.read(fi);
        int w = bis.getWidth();
        int h = bis.getHeight();
        int nw = 120;
        int nh = (nw * h) / w;
        if(nh>120) {
            nh = 120;
            nw = (nh * w) / h;
        }
        double sx = (double)nw / w;
        double sy = (double)nh / h;
        transform.setToScale(sx,sy);
        AffineTransformOp ato = new AffineTransformOp(transform, null);
        BufferedImage bid = new BufferedImage(nw, nh, BufferedImage.TYPE_3BYTE_BGR);
        ato.filter(bis,bid);
        ImageIO.write(bid, "jpeg", fo);
	}
}
