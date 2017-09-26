package com.dmsys.dropbox.utils;

import com.dmsys.airdiskpro.BrothersApplication;
import com.dmsys.dropbox.api.DMDropboxAPI;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A simple, tiny, nicely embeddable HTTP 1.0 server in Java Modified from
 * NanoHTTPD, you can find it here http://elonen.iki.fi/code/nanohttpd/
 */
public class DropBoxHttpServer {
	private String TAG = "dropboxHttpServer";
	private final static int MAX_THREAD_CNT = 50;
	private ExecutorService threadpool = null;

	private static final OkHttpClient mOkHttpClient = new OkHttpClient();
	public static final long DEFAULT_FILESIZE = 204800; // 200K

	static {
		mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
		File mFile = new File(BrothersApplication.getInstance().getCacheDir(),
				"dropbox_vod_cache");
		Cache cache = new Cache(mFile, 1024 * 1024 * 50); // 50Mb
		mOkHttpClient.setCache(cache);
	}

	// ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.
	 * <p>
	 *
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @param uri
	 *            Percent-decoded URI without parameters, for example
	 *            "/index.cgi"
	 * @param method
	 *            "GET", "POST" etc.
	 * @param parms
	 *            Parsed, percent decoded parameters from URI and, in case of
	 *            POST, data.
	 * @param header
	 *            Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 * 
	 *         通过uri 找到对应的文件 打包成response 返回出去
	 */
	public Response serve(String uri, String method, Properties header,
			Properties parms, Properties files) {
		String itemId = uri.replaceFirst("/", "");
		itemId = URLDecoder.decode(itemId);
		String newUri = DropBoxVodUrlConversionHelper.getInstance()
				.getRealUrlById(itemId);
		if (newUri != null) {
			uri = newUri;
		}
		if (newUri == null) {
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT,
					"Error 404, file not found.");
		} else {
			return serveFile(uri, header, myRootDir, false);
		}

	}

	/**
	 * HTTP response. Return one of these from serve().
	 */
	public class Response {
		/**
		 * Default constructor: response = HTTP_OK, data = mime = 'null'
		 */
		public Response() {
			this.status = HTTP_OK;
		}

		/**
		 * Basic constructor.
		 */
		public Response(String status, String mimeType, InputStream data) {
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response(String status, String mimeType, String txt) {
			this.status = status;
			this.mimeType = mimeType;
			try {
				this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
			} catch (java.io.UnsupportedEncodingException uee) {
				uee.printStackTrace();
			}
		}

		/**
		 * Adds given line to the header.
		 */
		public void addHeader(String name, String value) {
			header.put(name, value);
		}

		public void addHeaderAll(Map<String, String> headers) {
			header.putAll(headers);
		}

		/**
		 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
		 */
		public String status;

		/**
		 * MIME type of content, e.g. "text/html"
		 */
		public String mimeType;

		/**
		 * Data of the response, may be null.
		 */
		public InputStream data;

		/**
		 * Headers for the HTTP response. Use addHeader() to add lines.
		 */
		public Properties header = new Properties();

		public long length = -1;
	}

	/**
	 * Some HTTP response status codes
	 */
	public static final String HTTP_OK = "200 OK",
			HTTP_PARTIALCONTENT = "206 Partial Content",
			HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
			HTTP_REDIRECT = "301 Moved Permanently",
			HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
			HTTP_BADREQUEST = "400 Bad Request",
			HTTP_INTERNALERROR = "500 Internal Server Error",
			HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	public static final HashMap<Integer, String> HttpCodeMap = new HashMap<Integer, String>() {
		{
			put(200, HTTP_OK);
			put(206, HTTP_PARTIALCONTENT);
			put(416, HTTP_RANGE_NOT_SATISFIABLE);
			put(301, HTTP_REDIRECT);
			put(403, HTTP_FORBIDDEN);
			put(400, HTTP_BADREQUEST);
			put(500, HTTP_INTERNALERROR);
			put(501, HTTP_NOTIMPLEMENTED);
		}
	};

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain",
			MIME_HTML = "text/html",
			MIME_DEFAULT_BINARY = "application/octet-stream",
			MIME_XML = "text/xml";

	// ==================================================
	// Socket & server code
	// ==================================================
	private Future mFuture;

	/**
	 * Starts a HTTP server to given port.
	 * <p>
	 * Throws an IOException if the socket is already in use
	 */
	public DropBoxHttpServer(int port) throws IOException {
		threadpool = Executors.newFixedThreadPool(MAX_THREAD_CNT);
		myTcpPort = port;
		this.myRootDir = new File("/");
		myServerSocket = new ServerSocket(myTcpPort);
		hSessionServer = new HTTPSessionServer();
		addThreadPool(hSessionServer);
	}

	public synchronized Future addThreadPool(Runnable runnable) {
		if (threadpool == null) {
			threadpool = Executors.newFixedThreadPool(MAX_THREAD_CNT);
		}
		return threadpool.submit(runnable);
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		try {
			if (mOkHttpClient != null) {
				mOkHttpClient.cancel(TAG);
			}
		} catch (Exception e) {
		}
		try {
			if (myServerSocket != null) {
				myServerSocket.close();
				myServerSocket = null;
			}
		} catch (Exception e) {
		}
		try {
			if (threadpool != null) {
				threadpool.shutdownNow();
				threadpool = null;
			}
		} catch (Exception ioe) {
		}
	}

	public class HTTPSessionServer implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					new HTTPSession(myServerSocket.accept());
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Handles one session, i.e. parses the HTTP request and returns the
	 * response. 内部类
	 */
	private class HTTPSession implements Runnable {
		private Socket mySocket;

		public HTTPSession(Socket s) {
			mySocket = s;
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public void run() {
			try {
				InputStream is = mySocket.getInputStream();
				if (is == null)
					return;

				// Read the first 8192 bytes.
				// The full header should fit in here.
				// Apache's default header limit is 8KB.
				// 先读个8K后面继续读
				int bufsize = 8192;
				byte[] buf = new byte[bufsize];
				int rlen = is.read(buf, 0, bufsize);
				if (rlen <= 0)
					return;

				// Create a BufferedReader for parsing the header.
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0,
						rlen);
				BufferedReader hin = new BufferedReader(new InputStreamReader(
						hbis));
				Properties pre = new Properties();
				Properties parms = new Properties();
				Properties header = new Properties();
				Properties files = new Properties();

				// Decode the header into parms and header java properties
				decodeHeader(hin, pre, parms, header);

				String method = pre.getProperty("method");
				String uri = pre.getProperty("uri");

				long size = 0x7FFFFFFFFFFFFFFFl;
				String contentLength = header.getProperty("content-length");
				if (contentLength != null) {
					try {
						size = Integer.parseInt(contentLength);
					} catch (NumberFormatException ex) {
					}
				}

				// We are looking for the byte separating header from body.
				// It must be the last byte of the first two sequential new
				// lines.
				int splitbyte = 0;
				boolean sbfound = false;
				while (splitbyte < rlen) {
					if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n'
							&& buf[++splitbyte] == '\r'
							&& buf[++splitbyte] == '\n') {
						sbfound = true;
						break;
					}
					splitbyte++;
				}
				splitbyte++;

				// Write the part of body already read to ByteArrayOutputStream
				// f
				ByteArrayOutputStream f = new ByteArrayOutputStream();
				if (splitbyte < rlen)
					f.write(buf, splitbyte, rlen - splitbyte);

				// While Firefox sends on the first read all the data fitting
				// our buffer, Chrome and Opera sends only the headers even if
				// there is data for the body. So we do some magic here to find
				// out whether we have already consumed part of body, if we
				// have reached the end of the data to be sent or we should
				// expect the first byte of the body at the next read.
				if (splitbyte < rlen)
					size -= rlen - splitbyte + 1;
				else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl)
					size = 0;

				// Now read all the body and write it to f
				// 再读取个512byte
				buf = new byte[512];
				while (rlen >= 0 && size > 0) {
					rlen = is.read(buf, 0, 512);
					size -= rlen;
					if (rlen > 0)
						f.write(buf, 0, rlen);
				}
				// Get the raw body as a byte []
				byte[] fbuf = f.toByteArray();

				// Create a BufferedReader for easily reading it as string.
				ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						bin));
				// 这里开始读请求uri 的地址转换成文件流
				Response r = serve(uri, method, header, parms, files);
				if (r == null) {
					sendError(HTTP_INTERNALERROR,
							"SERVER INTERNAL ERROR: Serve() returned a null response.");
				} else {
					sendResponse(r.status, r.mimeType, r.header, r.data);
				}
				in.close();
				is.close();
			} catch (IOException ioe) {
				try {
					sendError(
							HTTP_INTERNALERROR,
							"SERVER INTERNAL ERROR: IOException: "
									+ ioe.getMessage());
				} catch (Throwable t) {
				}
			} catch (InterruptedException ie) {
				// Thrown by sendError, ignore and exit the thread.
			}
		}

		/**
		 * Decodes the sent headers and loads the data into java Properties' key
		 * - value pairs
		 **/
		private void decodeHeader(BufferedReader in, Properties pre,
				Properties parms, Properties header)
				throws InterruptedException {
			try {
				// Read the request line
				String inLine = in.readLine();
				if (inLine == null)
					return;
				StringTokenizer st = new StringTokenizer(inLine);
				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST,
							"BAD REQUEST: Syntax error. Usage: GET /example/file.html");

				String method = st.nextToken();
				pre.put("method", method);

				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST,
							"BAD REQUEST: Missing URI. Usage: GET /example/file.html");

				String uri = st.nextToken();

				// Decode parameters from the URI
				int qmi = uri.indexOf('?');
				if (qmi >= 0) {
					decodeParms(uri.substring(qmi + 1), parms);
					uri = decodePercent(uri.substring(0, qmi));
				} else
					uri = decodePercent(uri);

				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				// NOTE: this now forces header names lowercase since they are
				// case insensitive and vary by client.
				if (st.hasMoreTokens()) {
					String line = in.readLine();
					while (line != null && line.trim().length() > 0) {
						int p = line.indexOf(':');
						if (p >= 0)
							header.put(line.substring(0, p).trim()
									.toLowerCase(), line.substring(p + 1)
									.trim());
						line = in.readLine();
					}
				}

				pre.put("uri", uri);
			} catch (IOException ioe) {
				sendError(
						HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage());
			}
		}

		

		

	

		
		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent(String str) throws InterruptedException {
			try {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					switch (c) {
					case '+':
						sb.append(' ');
						break;
					case '%':
						sb.append((char) Integer.parseInt(
								str.substring(i + 1, i + 3), 16));
						i += 2;
						break;
					default:
						sb.append(c);
						break;
					}
				}
				return sb.toString();
			} catch (Exception e) {
				sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format ( e.g.
		 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
		 * Properties. NOTE: this doesn't support multiple identical keys due to
		 * the simplicity of Properties -- if you need multiples, you might want
		 * to replace the Properties with a Hashtable of Vectors or such.
		 */
		private void decodeParms(String parms, Properties p)
				throws InterruptedException {
			if (parms == null)
				return;

			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				if (sep >= 0)
					p.put(decodePercent(e.substring(0, sep)).trim(),
							decodePercent(e.substring(sep + 1)));
			}
		}

		/**
		 * Returns an error message as a HTTP response and throws
		 * InterruptedException to stop further request processing.
		 */
		private void sendError(String status, String msg)
				throws InterruptedException {
			sendResponse(status, MIME_PLAINTEXT, null,
					new ByteArrayInputStream(msg.getBytes()));
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse(String status, String mime,
				Properties header, InputStream data) {
			OutputStream out = null;
			try {
				if (status == null)
					throw new Error("sendResponse(): Status can't be null.");
				
				out = mySocket.getOutputStream();
				StringBuffer headers   = new StringBuffer("HTTP/1.1 " + status + " \r\n");

				if (header != null) {
					Enumeration e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = header.getProperty(key);
						headers.append(key + ": " + value + " \r\n") ;
					}
				}
				headers.append("\r\n");
				byte[] buffers = headers.toString().getBytes();
				out.write(buffers,0,buffers.length);
				out.flush();

				if (data != null) {
					int readLen = -1;
					byte[] buff = new byte[16384];
					while ((readLen = data.read(buff)) > 0) {
						out.write(buff, 0, readLen);
					}
				}
				out.flush();
				out.close();
				out = null;
				if (data != null) {
					data.close();
					data = null;
				}

			} catch (IOException e) {
				// Couldn't write? No can do.

			} finally {
				if (data != null) {
					try {
						data.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					data = null;
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					out = null;
				}
				try {
					if (mySocket != null) {
						mySocket.close();
						mySocket = null;
					}
				} catch (Throwable t) {
				}

			}
		}

	}


	private int myTcpPort;
	private ServerSocket myServerSocket;
	private HTTPSessionServer hSessionServer;
	private File myRootDir;
	boolean bRunnable = true;

	// ==================================================
	// File server code
	// ==================================================

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters. 通过映射的地址得出此文家你的输出输入流
	 */
	public Response serveFile(String uri, Properties header, File homeDir,
			boolean allowDirectoryListing) {
		Response res = null;
		com.squareup.okhttp.Request.Builder mBuilder = new Request.Builder()
				.url(uri).tag(TAG);
		if (header != null && header.size() > 0) {
			for (Entry<Object, Object> entry : header.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					if (entry.getKey().equals("host"))
						continue;
					mBuilder.header((String) entry.getKey(),
							(String) entry.getValue());
				}

			}
		}
		/**
		 * 添加dropBox 的验证信息
		 */
		HashMap<String, String> headers = new HashMap<String, String>();
		DMDropboxAPI.getInstance().getSession().sign(headers);
		mBuilder.header("Authorization", headers.get("Authorization"));
		Request request = mBuilder.build();
		com.squareup.okhttp.Response response = null;
		try {
			response = mOkHttpClient.newCall(request).execute();
			if (response != null) {
				res = new Response(HttpCodeMap.get(response.code()), null,
						response.body().byteStream());

//				for (Map.Entry<String, List<String>> entry : response.headers()
//						.toMultimap().entrySet()) {
//					if (entry.getKey() != null && entry.getValue() != null) {
//						res.addHeader(entry.getKey(),
//								Arrays.toString(entry.getKey().toCharArray()));
//					}
//				}
				 res.addHeader("Content-Length",response.header("Content-Length"));
				 if (response.header("content-range") != null) {
					 res.addHeader("Content-Range",response.header("content-range"));
				 }
				 res.addHeader("Cache-Control",response.header("cache-control"));
				 res.addHeader("ETag", response.header("etag"));
				 res.addHeader("Content-Type",response.header("Content-Type"));
				 res.addHeader("Server", response.header("Server"));
				 res.addHeader("Date", response.header("Date"));
				 res.addHeader("Connection", response.header("Connection"));
			} else {
				res = new Response(HTTP_NOTFOUND, MIME_PLAINTEXT,
						"Error 404, file not found.");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Hashtable theMimeTypes = new Hashtable();
	static {
		StringTokenizer st = new StringTokenizer("css		text/css "
				+ "js			text/javascript " + "htm		text/html "
				+ "html		text/html " + "txt		text/plain " + "asc		text/plain "
				+ "gif		image/gif " + "jpg		image/jpeg " + "jpeg		image/jpeg "
				+ "png		image/png " + "mp3		audio/mpeg "
				+ "m3u		audio/mpeg-url " + "pdf		application/pdf "
				+ "doc		application/msword " + "ogg		application/x-ogg "
				+ "zip		application/octet-stream "
				+ "exe		application/octet-stream "
				+ "class		application/octet-stream ");
		while (st.hasMoreTokens())
			theMimeTypes.put(st.nextToken(), st.nextToken());
	}

	/**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;
	static {
		gmtFrmt = new java.text.SimpleDateFormat(
				"E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

}
