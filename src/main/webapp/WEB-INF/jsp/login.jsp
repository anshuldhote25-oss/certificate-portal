<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign in"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<section class="card narrow">
  <h1>Sign in</h1>
  <c:if test="${not empty error}">
    <div class="alert" role="alert" data-testid="login-error"><c:out value="${error}"/></div>
  </c:if>
  <form method="post" action="${ctx}/login">
    <label for="username">Username</label>
    <input id="username" name="username" type="text" autocomplete="username" required>
    <label for="password">Password</label>
    <input id="password" name="password" type="password" autocomplete="current-password" required>
    <button type="submit" class="primary" data-testid="login-submit">Sign in</button>
  </form>
  <p class="hint">Demo accounts: operator / operator123, approver / approver123, admin / admin123</p>
</section>
<%@ include file="/WEB-INF/jsp/footer.jspf" %>
