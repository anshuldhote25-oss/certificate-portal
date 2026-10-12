<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${formTitle}"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<section class="card narrow">
  <h1><c:out value="${formTitle}"/></h1>
  <form method="post" action="${formAction}" novalidate>
    <input type="hidden" name="_csrf" value="${sessionScope.csrf}">

    <label for="recipientName">Recipient name</label>
    <input id="recipientName" name="recipientName" type="text" value="<c:out value='${certificate.recipientName}'/>">
    <c:if test="${not empty errors.recipientName}"><span class="error" data-testid="error-recipientName"><c:out value="${errors.recipientName}"/></span></c:if>

    <label for="recipientEmail">Recipient email</label>
    <input id="recipientEmail" name="recipientEmail" type="text" value="<c:out value='${certificate.recipientEmail}'/>">
    <c:if test="${not empty errors.recipientEmail}"><span class="error" data-testid="error-recipientEmail"><c:out value="${errors.recipientEmail}"/></span></c:if>

    <label for="course">Course</label>
    <input id="course" name="course" type="text" value="<c:out value='${certificate.course}'/>">
    <c:if test="${not empty errors.course}"><span class="error" data-testid="error-course"><c:out value="${errors.course}"/></span></c:if>

    <label for="issueDate">Issue date (YYYY-MM-DD)</label>
    <input id="issueDate" name="issueDate" type="text" placeholder="2026-10-12" value="<c:out value='${certificate.issueDate}'/>">
    <c:if test="${not empty errors.issueDate}"><span class="error" data-testid="error-issueDate"><c:out value="${errors.issueDate}"/></span></c:if>

    <label for="remarks">Remarks (optional)</label>
    <textarea id="remarks" name="remarks" rows="3"><c:out value="${certificate.remarks}"/></textarea>

    <div class="actions">
      <button type="submit" class="primary" data-testid="save">Save</button>
      <a class="button" href="${ctx}/certificates">Cancel</a>
    </div>
  </form>
</section>
<%@ include file="/WEB-INF/jsp/footer.jspf" %>
