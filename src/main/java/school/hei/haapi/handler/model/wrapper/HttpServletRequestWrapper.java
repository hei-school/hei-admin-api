package school.hei.haapi.handler.model.wrapper;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ReadListener;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.handler.model.requestEvent.LambdaUrlRequestEvent;

@PojaGenerated
@SuppressWarnings("all")
public class HttpServletRequestWrapper implements HttpServletRequest {
  private final LambdaUrlRequestEvent requestEvent;
  private final Map<String, Object> attributes;

  static String CHARACTER_ENCODING = "UTF-8";

  public HttpServletRequestWrapper(LambdaUrlRequestEvent requestEvent) {
    this.requestEvent = requestEvent;
    this.attributes = new HashMap<>();
  }

  @Override
  public String getAuthType() {
    return "";
  }

  @Override
  public Cookie[] getCookies() {
    List<String> rawCookies = requestEvent.getCookies();
    if (rawCookies == null || rawCookies.isEmpty()) return null;

    List<Cookie> cookies = new ArrayList<>();
    for (String rawCookie : rawCookies) {
      String[] parts = rawCookie.split("=", 2);
      if (parts.length == 2) {
        cookies.add(new Cookie(parts[0].trim(), parts[1].trim()));
      }
    }
    return cookies.toArray(new Cookie[0]);
  }

  @Override
  public long getDateHeader(String s) {
    return 0;
  }

  @Override
  public String getHeader(String s) {
    if (requestEvent.getHeaders().containsKey(s)) {
      return requestEvent.getHeaders().get(s);
    }
    return null;
  }

  @Override
  public Enumeration<String> getHeaders(String s) {
    String HeaderValue = requestEvent.getHeaders().get(s);
    return HeaderValue == null
        ? Collections.emptyEnumeration()
        : Collections.enumeration(Arrays.asList(HeaderValue.split(",")));
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return requestEvent.getHeaders() == null
        ? Collections.emptyEnumeration()
        : Collections.enumeration(requestEvent.getHeaders().keySet());
  }

  @Override
  public int getIntHeader(String s) {
    return 0;
  }

  @Override
  public String getMethod() {
    return requestEvent.getRequestContext().getHttp().getMethod();
  }

  @Override
  public String getPathInfo() {
    return "";
  }

  @Override
  public String getPathTranslated() {
    return "";
  }

  @Override
  public String getContextPath() {
    return "";
  }

  @Override
  public String getQueryString() {
    return requestEvent.getRawQueryString();
  }

  @Override
  public String getRemoteUser() {
    return "";
  }

  @Override
  public boolean isUserInRole(String s) {
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    return "";
  }

  @Override
  public String getRequestURI() {
    return requestEvent.getRequestContext().getHttp().getPath();
  }

  @Override
  public StringBuffer getRequestURL() {
    String protocol = getScheme().isEmpty() ? "https" : getScheme();
    String host = requestEvent.getHeaders().getOrDefault("Host", "localhost");
    String path = requestEvent.getRawPath();
    return new StringBuffer(protocol + "://" + host + path);
  }

  @Override
  public String getServletPath() {
    return "";
  }

  @Override
  public HttpSession getSession(boolean b) {
    return null;
  }

  @Override
  public HttpSession getSession() {
    return null;
  }

  @Override
  public String changeSessionId() {
    return "";
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse httpServletResponse) {
    return false;
  }

  @Override
  public void login(String s, String s1) {}

  @Override
  public void logout() {}

  @Override
  public Collection<Part> getParts() {
    return List.of();
  }

  @Override
  public Part getPart(String s) {
    return null;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) {
    return null;
  }

  @Override
  public Object getAttribute(String s) {
    return attributes.get(s);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(attributes.keySet());
  }

  @Override
  public String getCharacterEncoding() {

    return CHARACTER_ENCODING;
  }

  @Override
  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    if (s == null || !Charset.isSupported(CHARACTER_ENCODING)) {
      throw new UnsupportedEncodingException("Unsupported Encoding :" + s);
    }

    CHARACTER_ENCODING = s;
  }

  @Override
  public int getContentLength() {
    return 0;
  }

  @Override
  public long getContentLengthLong() {
    return 0;
  }

  @Override
  public String getContentType() {
    return "";
  }

  @Override
  public ServletInputStream getInputStream() {
    byte[] bodyBytes =
        requestEvent.getBody() == null ? new byte[0] : requestEvent.getBody().getBytes();
    ByteArrayInputStream byteStream = new ByteArrayInputStream(bodyBytes);

    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return byteStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {}

      @Override
      public int read() {
        return byteStream.read();
      }
    };
  }

  @Override
  public String getParameter(String s) {
    String values = requestEvent.getQueryStringParameters().get(s);
    return values == null ? null : values.split(";")[0];
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return requestEvent.getQueryStringParameters() == null
        ? Collections.emptyEnumeration()
        : Collections.enumeration(requestEvent.getQueryStringParameters().keySet());
  }

  @Override
  public String[] getParameterValues(String s) {
    String values = requestEvent.getQueryStringParameters().get(s);
    return values == null ? null : values.split(";");
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> result = new HashMap<>();
    Map<String, String> params = requestEvent.getQueryStringParameters();
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        result.put(entry.getKey(), entry.getValue().split(","));
      }
    }
    return result;
  }

  @Override
  public String getProtocol() {
    return "";
  }

  @Override
  public String getScheme() {
    return "";
  }

  @Override
  public String getServerName() {
    return "";
  }

  @Override
  public int getServerPort() {
    return 0;
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  @Override
  public String getRemoteAddr() {
    return "";
  }

  @Override
  public String getRemoteHost() {
    return "";
  }

  @Override
  public void setAttribute(String s, Object o) {
    attributes.put(s, o);
  }

  @Override
  public void removeAttribute(String s) {
    attributes.remove(s);
  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return null;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  @Override
  public int getRemotePort() {
    return 0;
  }

  @Override
  public String getLocalName() {
    return "";
  }

  @Override
  public String getLocalAddr() {
    return "";
  }

  @Override
  public int getLocalPort() {
    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    return null;
  }

  @Override
  public String getRequestId() {
    return "";
  }

  @Override
  public String getProtocolRequestId() {
    return "";
  }

  @Override
  public ServletConnection getServletConnection() {
    return null;
  }
}
