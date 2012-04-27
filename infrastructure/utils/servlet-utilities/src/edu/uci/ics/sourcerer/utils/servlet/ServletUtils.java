package edu.uci.ics.sourcerer.utils.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletUtils {
  public static Integer getIntValue(HttpServletRequest request, String name) {
    String val = request.getParameter(name);
    if (val == null) {
      return null;
    } else {
      try {
        return Integer.valueOf(val);
      } catch (NumberFormatException e) {
        return null;
      }
    }
  }
  
	public static void writeEntityIds(HttpServletResponse response, long[] eids)
	throws IOException {
		OutputStream os = response.getOutputStream();
		
		if(eids==null || eids.length<1){
			os.write("Error : no entity ids to write".getBytes());
			os.close();
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		for(long eid: eids){
			sb.append(eid);
			sb.append(",");
		}
		String eidsStr = sb.substring(0,sb.length()-1);
		setResponse(response);
		
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

	private static void setResponse(HttpServletResponse response, String name, boolean link) {
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
	
	public static void writeString(HttpServletResponse response, String name, String string, boolean html) throws IOException {
	  writeByteArray(response, name, string.getBytes(), html);
	}

	public static void writeByteArray(HttpServletResponse response, String name, byte[] bytes) throws IOException {
	  writeByteArray(response, name, bytes, false);
	}
	
	public static void writeByteArray(HttpServletResponse response, String name, byte[] bytes, boolean html) throws IOException {
	  setResponse(response, name, html);

	  OutputStream os = response.getOutputStream();
	  os.write(bytes);
	  os.close();
	}
	
  public static void writeFile(HttpServletResponse response, String name, File file) throws IOException {
    writeInputStream(response, name, new FileInputStream(file));
  }

  public static void writeInputStream(HttpServletResponse response, String name, InputStream is) throws IOException {
    writeInputStream(response, name, is, false);
  }
  
  public static void writeInputStream(HttpServletResponse response, String name, InputStream is, boolean html) throws IOException {
    setResponse(response, name, html);

    OutputStream os = response.getOutputStream();

    byte[] buff = new byte[1024];
    int read = 0;
    while ((read = is.read(buff)) > 0) {
      os.write(buff, 0, read);
    }
    os.close();
    is.close();
  }

  public static void writeFileFragment(HttpServletResponse response, String name, File file, int offset, int length) throws IOException {
    writeInputStreamFragment(response, name, new FileInputStream(file), offset, length);
  }

  public static void writeInputStreamFragment(HttpServletResponse response, String name, InputStream is, int offset, int length) throws IOException {
    writeInputStreamFragment(response, name, is, offset, length, false);
  }
  
	public static void writeInputStreamFragment(HttpServletResponse response, String name, InputStream is, int offset, int length, boolean html) throws IOException {
		setResponse(response, name, html);

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
