package edu.uci.ics.sourcerer.util.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

public class ServletUtils {

	public static void writeEntityIds(HttpServletResponse response, long[] eids)
	throws IOException {
		StringBuilder sb = new StringBuilder();
		for(long eid: eids){
			sb.append(eid);
			sb.append(",");
		}
		String eidsStr = sb.substring(0,sb.length()-1);
		setResponse(response);
		OutputStream os = response.getOutputStream();
		os.write(eidsStr.getBytes());
		os.close();
 	}
	
	public static void writeErrorMsg(HttpServletResponse response, String msg)
			throws IOException {
		setResponse(response);
		OutputStream os = response.getOutputStream();
		os.write(msg.getBytes());
		os.close();
	}

	private static void setResponse(HttpServletResponse response) {
		setResponse(response, null, false);
	}

	private static void setResponse(HttpServletResponse response, boolean link) {
		setResponse(response, null, link);
	}

	private static void setResponse(HttpServletResponse response, String name) {
		setResponse(response, name, false);
	}

	private static void setResponse(HttpServletResponse response, String name,
			boolean link) {
		if (name == null) {
			if (link) {
				response.setContentType("text/html");
			} else {
				response.setContentType("text/plain");
			}
		} else {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment;filename=\""
					+ name + "\"");
		}
	}

	public static void writeFile(HttpServletResponse response, String name,
			File file) throws IOException {
		writeInputStream(response, name, new FileInputStream(file));
	}

	public static void writeInputStream(HttpServletResponse response,
			String name, InputStream is) throws IOException {
		setResponse(response, name);

		OutputStream os = response.getOutputStream();

		byte[] buff = new byte[1024];
		int read = 0;
		while ((read = is.read(buff)) > 0) {
			os.write(buff, 0, read);
		}
		os.close();
		is.close();
	}

	public static void writeFileFragment(HttpServletResponse response,
			String name, File file, int offset, int length) throws IOException {
		writeInputStreamFragment(response, name, new FileInputStream(file),
				offset, length);
	}

	public static void writeInputStreamFragment(HttpServletResponse response,
			String name, InputStream is, int offset, int length)
			throws IOException {
		setResponse(response, name);

		OutputStream os = response.getOutputStream();

		while (offset > 0) {
			offset -= is.skip(offset);
		}

		byte[] buff = new byte[length];
		int read = 0;
		while (read < length) {
			read += is.read(buff, read, length - read);
		}
		os.write(buff);
		os.close();
		is.close();
	}
}
