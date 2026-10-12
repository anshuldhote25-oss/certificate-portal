<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Error"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<section class="card narrow">
  <h1 data-testid="error-code">Error <c:out value="${requestScope['jakarta.servlet.error.status_code']}"/></h1>
  <p data-testid="error-message">
    <c:choose>
      <c:when test="${not empty requestScope['jakarta.servlet.error.message'] && requestScope['jakarta.servlet.error.status_code'] != 500}">
        <c:out value="${requestScope['jakarta.servlet.error.message']}"/>
      </c:when>
      <c:otherwise>Something went wrong. Please try again.</c:otherwise>
    </c:choose>
  </p>
  <a class="button" href="${ctx}/dashboard">Back to dashboard</a>
</section>
<%@ include file="/WEB-INF/jsp/footer.jspf" %>
