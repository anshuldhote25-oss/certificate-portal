<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${certificate.certNo}"/>
<%@ include file="/WEB-INF/jsp/header.jspf" %>
<section class="card">
  <h1><span data-testid="cert-no"><c:out value="${certificate.certNo}"/></span>
      <span class="badge ${certificate.status.css}" data-testid="status"><c:out value="${certificate.status.label}"/></span></h1>
  <dl>
    <dt>Recipient</dt><dd data-testid="name"><c:out value="${certificate.recipientName}"/></dd>
    <dt>Email</dt><dd data-testid="email"><c:out value="${certificate.recipientEmail}"/></dd>
    <dt>Course</dt><dd data-testid="course"><c:out value="${certificate.course}"/></dd>
    <dt>Issue date</dt><dd data-testid="issue-date"><c:out value="${certificate.issueDate}"/></dd>
    <dt>Remarks</dt><dd data-testid="remarks"><c:out value="${certificate.remarks}"/></dd>
    <dt>Created by</dt><dd><c:out value="${certificate.createdBy}"/></dd>
    <dt>Last updated by</dt><dd data-testid="updated-by"><c:out value="${certificate.updatedBy}"/></dd>
  </dl>

  <div class="actions">
    <a class="button" href="${ctx}/certificates" data-testid="back-link">Back to list</a>
  </div>
</section>
<%@ include file="/WEB-INF/jsp/footer.jspf" %>
