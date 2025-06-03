package school.hei.haapi.handler.model.wrapper;

import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
public class HttpServletResponseWrapper implements HttpServletResponse {
  private final ByteArrayOutputStream outputStream;
  private final Map<String, List<String>> headers;
  private int status;
  private String characterEncoding;
  private String contentType;

  public HttpServletResponseWrapper(
      ByteArrayOutputStream outputStream, Map<String, List<String>> headers) {
    this.outputStream = outputStream;
    this.headers = headers;
    this.status = SC_OK;
  }

  @Override
  public void addCookie(Cookie cookie) {}

  @Override
  public boolean containsHeader(String s) {
    return headers.containsKey(s);
  }

  @Override
  public String encodeURL(String s) {
    return s;
  }

  @Override
  public String encodeRedirectURL(String s) {
    return s;
  }

  @Override
  public void sendError(int i, String s) throws IOException {
    this.status = i;
    this.outputStream.write(s.getBytes());
  }

  @Override
  public void sendError(int i) {
    this.status = i;
  }

  @Override
  public void sendRedirect(String s) {
    this.status = SC_MOVED_TEMPORARILY;
    this.headers.put(LOCATION, List.of(s));
  }

  @Override
  public void setDateHeader(String s, long l) {
    this.headers.put(s, List.of(String.valueOf(l)));
  }

  @Override
  public void addDateHeader(String s, long l) {
    this.headers.put(s, List.of(String.valueOf(l)));
  }

  @Override
  public void setHeader(String s, String s1) {
    this.headers.put(s, List.of(s1));
  }

  @Override
  public void addHeader(String s, String s1) {
    this.headers.computeIfAbsent(s, k -> new ArrayList<>()).add(s1);
  }

  @Override
  public void setIntHeader(String s, int i) {
    this.headers.put(s, List.of(String.valueOf(i)));
  }

  @Override
  public void addIntHeader(String s, int i) {
    this.headers.computeIfAbsent(s, k -> new ArrayList<>()).add(String.valueOf(i));
  }

  @Override
  public void setStatus(int i) {
    this.status = i;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public String getHeader(String s) {
    List<String> values = headers.get(s);
    return (values != null && !values.isEmpty()) ? values.getFirst() : null;
  }

  @Override
  public Collection<String> getHeaders(String s) {
    return headers.getOrDefault(s, List.of());
  }

  @Override
  public Collection<String> getHeaderNames() {
    return headers.keySet();
  }

  @Override
  public String getCharacterEncoding() {
    return characterEncoding != null ? characterEncoding : "UTF-8";
  }

  @Override
  public String getContentType() {
    return contentType != null ? contentType : "application/json";
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {}

      @Override
      public void write(int b) {
        outputStream.write(b);
      }
    };
  }

  @Override
  public PrintWriter getWriter() {
    return new PrintWriter(outputStream);
  }

  @Override
  public void setCharacterEncoding(String s) {
    this.characterEncoding = s;
  }

  @Override
  public void setContentLength(int i) {}

  @Override
  public void setContentLengthLong(long l) {}

  @Override
  public void setContentType(String s) {
    this.contentType = s;
    headers.put(CONTENT_TYPE, List.of(s));
  }

  @Override
  public void setBufferSize(int i) {}

  @Override
  public int getBufferSize() {
    return outputStream.size();
  }

  @Override
  public void flushBuffer() throws IOException {
    outputStream.flush();
  }

  @Override
  public void resetBuffer() {
    outputStream.reset();
  }

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {
    this.status = SC_RESET_CONTENT;
    this.headers.clear();
    this.outputStream.reset();
  }

  @Override
  public void setLocale(Locale locale) {
    this.headers.put(CONTENT_LANGUAGE, List.of(locale.toLanguageTag()));
  }

  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }
}
